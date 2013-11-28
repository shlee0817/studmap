using System;
using System.Web.Http;
using StudMap.Core;
using StudMap.Core.Graph;
using StudMap.Core.WLAN;
using StudMap.Data.Entities;
using System.Linq;
using MathNet.Numerics.Distributions;
using System.Collections.Generic;
using System.Web;
using StudMap.Service.App_Start;
using StudMap.Service.CacheObjects;

namespace StudMap.Service.Controllers
{
    public class FingerprintController : ApiController
    {
        [HttpPost]
        public BaseResponse SaveFingerprintForNode(int nodeId, Fingerprint fingerprint)
        {
            var result = new BaseResponse();

            if (fingerprint == null)
            {
                result.SetError(ResponseError.FingeprintIsNotDefined);
                return result;
            } 
            if (fingerprint.AccessPointScans == null || !fingerprint.AccessPointScans.Any())
                return result;

            try
            {
                using (var entities = new MapsEntities())
                {
                    var node = entities.Nodes.FirstOrDefault(x => x.Id == nodeId);
                    if (node == null)
                    {
                        result.SetError(ResponseError.NodeIdDoesNotExist);
                        return result;
                    }

                    var fingerprints = entities.Fingerprints.Add(new Fingerprints
                        {
                            NodeId = node.Id
                        });

                    entities.SaveChanges();

                    foreach (var apScan in fingerprint.AccessPointScans)
                    {
                        var ap = entities.AccessPoints.FirstOrDefault(x => x.MAC == apScan.AccessPoint.MAC);
                        if (ap == null)
                        {
                            ap = entities.AccessPoints.Add(new AccessPoints
                                {
                                    MAC = apScan.AccessPoint.MAC
                                });

                            entities.SaveChanges();
                        }

                        entities.AccessPointScans.Add(new AccessPointScans
                            {
                                AccessPointId = ap.Id,
                                RecievedSignalStrength = apScan.ReceivedSignalStrength,
                                FingerprintId = fingerprints.Id
                            });
                    }

                    entities.SaveChanges();

                    CacheConfig.RemoveNodeDistribution();
                }
            }
            catch (Exception e)
            {
                result.SetError(ResponseError.DatabaseError);
                Elmah.ErrorSignal.FromCurrentContext().Raise(e);
            }
            return result;
        }

        [HttpPost]
        public ObjectResponse<Node> GetNodeForFingerprint(Fingerprint fingerprint, double factor)
        {
            return new ObjectResponse<Node>();
        }

        [HttpPost]
        public ListResponse<NodeProbability> GetNodeProbabiltyForScan(LocationRequest request)
        {
            var response = new ListResponse<NodeProbability>();

            using (var entities = new MapsEntities())
            {
                // Gesammelte WLAN-Fingerprints aus DB auswerten und Verteilung bestimmen
                // Jetzt: gecachet!
                var nodeDistributionCache = (NodeDistributionCache)HttpRuntime.Cache.Get(CacheConfig.NODE_DISTRIBUTION_CACHE);
                if (nodeDistributionCache == null)
                    nodeDistributionCache = CacheConfig.RegisterNodeDistribution();

                var nodeDistributions = nodeDistributionCache.NodeDistributions;

                // AccessPoint-Messung nach APs aufteilen
                var apScans = AnalyseInputScan(request, entities);

                // W'keit bestimmen, dass RSS-Werte an Knoten gemessen werden
                var nodeProbs = CalculateNodeProbabilities(nodeDistributions, apScans);

                // Absteigend nach W'keit sortieren
                nodeProbs.Sort((m, n) => m.Probabilty.CompareTo(n.Probabilty));

                // Maximal die angeforderte Anzahl an Knoten zurückliefern
                int count = Math.Min(request.NodeCount, nodeProbs.Count);
                response.List = nodeProbs.GetRange(0, count);
            }

            return response;
        }

        private static List<NodeProbability> CalculateNodeProbabilities(Dictionary<int, Dictionary<int, Normal>> nodeDistributions, Dictionary<int, int> apScans)
        {
            var nodeProbs = new List<NodeProbability>();
            foreach (var nodeId in nodeDistributions.Keys)
            {
                var apDistributions = nodeDistributions[nodeId];
                double prob = 1.0;
                int relevantApCount = 0;
                foreach (var apId in apDistributions.Keys)
                {
                    int scannedValue;
                    // Befindet sich der in der DB hinterlegte AP im aktuellen Fingerprint?
                    if (apScans.TryGetValue(apId, out scannedValue))
                    {
                        // Wenn ja, dann kann die W'keit für den gemessenen
                        // Wert aus der Normalverteilung bestimmt werden
                        var dist = apDistributions[apId];
                        double before = dist.CumulativeDistribution(scannedValue - 0.5);
                        double after = dist.CumulativeDistribution(scannedValue + 0.5);

                        // Wie wahrscheinlich ist es, am aktuellen Knoten & AP, den gemessenen RSS-Wert vorzufinden?
                        double apProb = after - before;

                        // In die Gesamtw'keit für den Fingeprint einrechnen
                        prob *= apProb;
                        relevantApCount += 1;
                    }
                }

                // Wenn keine übereinstimmende APs gefunden wurden, dann wird
                // der Knoten ignoriert
                if (relevantApCount == 0)
                    continue;

                // Gesamtw'keit berechnen, am aktuellen Knoten, die gemessenen RSS-Werte
                // für alle AccessPoints vorzufinden
                // Mathematisch: Geometrisches Mittel über die Einzelw'keiten
                prob = Math.Pow(prob, 1.0 / relevantApCount);

                // Die hier berechnete bedingte W'keit ist noch "verkehrt herum".
                // Wir haben berechnet:
                //  p(RSS/n):
                //  W'keit, dass die RSS Werte gemessen werden, wenn die Messung
                //  an dem Knoten n durchgeführt wurde
                //
                // Wir suchen:
                //  p(n/RSS):
                //  W'keit, dass wir uns an dem Knoten n befinden, wenn die
                //  Messung die angegebenen RSS-Werte lieferte
                //
                // Umrechung:
                //             p(RSS/n) * p(n)
                //  p(n/RSS) = ---------------
                //                 p(RSS)
                // Wobei:
                //  p(n): W'keit, sich an einem Knoten zu befinden, unabhängig von RSS-Werten
                //    Hier macht initial so etwas wie 1/(Anzahl Knoten) Sinn (Gleichverteilung)
                //    Später können "nahe" Knoten zur vorherigen Position höhere W'keiten
                //    bekommen und so den Algorithmus verbessern
                //
                //  p(RSS): W'keit, dass die angegebenen RSS-Werte gemessen werden, 
                //    unabhängig von irgendwelchen Knoten/Positionen
                //  TODO: Prüfen, ob es Sinn macht, hier AVG + STDEV über alle Datensätze
                //        zu verwenden und für jeden AP eine Normalverteilung zu verwenden
                // 
                // ACHTUNG: Die oben beschriebene Korrektur ist ohne positionsabhängige p(n)
                //  nur aus mathematischen Gründen notwendig. Wenn p(n) = const, dann können
                //  diese Werte ignoriert werden, da sie einen konstanten Korrekturfaktor
                //  bilden, der die Größenrelation der W'keiten untereinander nicht ändert!
                //
                //   Wenn p(n) = const, dann p(n/RSS) = p(RSS/n) * const
                //
                //  Die errechneten W'keiten können somit nur zum Vergleich untereinander
                //  für eine Messung verwendet werden. Um zu bestimmen, welche Knoten am
                //  wahrscheinlichsten sind, reicht dies aus.

                // TODO: Wenn p(n) positionsabhängig wird, dann hier verwenden
                // double nodeProb = 1.0 / nodeDistributions.Count; // Konstant => ignorieren
                double correctedProb = prob;

                var nodeProb = new NodeProbability { NodeId = nodeId, Probabilty = correctedProb };
                nodeProbs.Add(nodeProb);
            }
            return nodeProbs;
        }

        private static Dictionary<int, int> AnalyseInputScan(LocationRequest request, MapsEntities entities)
        {
            var MACtoApId = entities.AccessPoints.ToDictionary(ap => ap.MAC, ap => ap.Id);

            var apScans = new Dictionary<int, int>();
            foreach (var scan in request.Scans)
            {
                int apId;
                if (MACtoApId.TryGetValue(scan.MAC, out apId))
                    apScans.Add(apId, scan.RSS);
            }
            return apScans;
        }

        private static Dictionary<int, Dictionary<int, Normal>> CalculateNodeDistributions(MapsEntities entities)
        {
            var nodeDistributions = new Dictionary<int, Dictionary<int, Normal>>();
            foreach (var dist in entities.RSSDistribution)
            {
                if (!nodeDistributions.ContainsKey(dist.NodeId))
                    nodeDistributions.Add(dist.NodeId, new Dictionary<int, Normal>());

                nodeDistributions[dist.NodeId].Add(dist.AccessPointId,
                    Normal.WithMeanStdDev(dist.AvgRSS ?? 0, dist.StDevRSS ?? 0.0));
            }
            return nodeDistributions;
        }
    }
}
