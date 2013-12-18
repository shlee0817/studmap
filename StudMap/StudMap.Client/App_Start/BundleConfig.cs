using System.Web.Optimization;

namespace StudMap.Client.App_Start
{
    public class BundleConfig
    {
        // For more information on Bundling, visit http://go.microsoft.com/fwlink/?LinkId=254725
        public static void RegisterBundles(BundleCollection bundles)
        {
            bundles.Add(new ScriptBundle("~/bundles/jquery").Include(
                "~/Scripts/jquery-1.*"));

            bundles.Add(new ScriptBundle("~/bundles/jqueryval").Include(
                "~/Scripts/jquery.validate*"));

            bundles.Add(new ScriptBundle("~/bundles/modernizr").Include(
                "~/Scripts/modernizr-*"));

            bundles.Add(new ScriptBundle("~/bundles/StudMapClient").Include(
                "~/Scripts/floorplan.js",
                "~/Scripts/imagelayer.js",
                "~/Scripts/graph.js",
                "~/Scripts/pathplot.js",
                "~/Scripts/Client.js"));

            bundles.Add(new StyleBundle("~/Content/css").Include("~/Content/site.css"));

            BundleTable.EnableOptimizations = true;
        }
    }
}