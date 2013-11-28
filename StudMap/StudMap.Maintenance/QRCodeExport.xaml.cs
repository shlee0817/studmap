using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Windows;
using System.Windows.Input;
using Gma.QrCodeNet.Encoding;
using Gma.QrCodeNet.Encoding.Windows.Render;
using Newtonsoft.Json;
using StudMap.Core.Information;
using StudMap.Maintenance.Models;
using StudMap.Service.Controllers;

namespace StudMap.Maintenance
{
    /// <summary>
    /// Interaction logic for QRCodeExport.xaml
    /// </summary>
    public partial class QRCodeExport
    {
        private ObservableCollection<QRCodeNode> Nodes = new ObservableCollection<QRCodeNode>();

        public ObservableCollection<QRCodeNode> ObservableNodes { get { return Nodes; } }

        public QRCodeExport()
        {
            InitializeComponent();
            Init();
            DataContext = this;
        }

        private void Init()
        {
            Mouse.OverrideCursor = Cursors.Wait;
            try
            {
                Nodes.Clear();
                var mapsCtrl = new MapsController();
                var nodeResponse = mapsCtrl.GetNodeInformation(0, 0);
                foreach (var node in nodeResponse.List)
                {
                    Nodes.Add(new QRCodeNode(node));
                }
            }
            finally
            {
                Mouse.OverrideCursor = null;
            }
        }

        private void btnGenerate_Click(object sender, RoutedEventArgs e)
        {
            var enc = new QrEncoder(ErrorCorrectionLevel.M);
            var mapsCtrl = new MapsController();

            var folder = Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
                "StudMap");

            if (!Directory.Exists(folder))
                Directory.CreateDirectory(folder);

            foreach (var node in Nodes)
            {
                var qrCode = new QRCode
                    {
                        General = new General
                            {
                                DisplayName = node.DisplayName,
                                RoomName = node.RoomName
                            },
                        StudMap = new Core.Information.StudMap
                            {
                                NodeId = node.NodeId
                            }
                    };
                var qrCodeStr = JsonConvert.SerializeObject(qrCode);
                try
                {
                    QrCode bmp;
                    enc.TryEncode(qrCodeStr, out bmp);
                    var path = Path.Combine(folder, node.DisplayName + "_" + node.RoomName + ".png");

                    var gRenderer = new GraphicsRenderer(
                        new FixedModuleSize(2, QuietZoneModules.Two),
                        Brushes.Black, Brushes.White);

                    var ms = new MemoryStream();
                    gRenderer.WriteToStream(bmp.Matrix, ImageFormat.Png, ms);
                    var output = new Bitmap(ms);
                    output.Save(path, ImageFormat.Png);
                }
                catch(Exception ex)
                {
                    MessageBox.Show(ex.Message);
                }
                finally
                {
                    mapsCtrl.SaveQRCodeForNode(node.NodeId, qrCodeStr);
                }
            }

            Process.Start(folder);
        }

        private void btnFilter_Click(object sender, RoutedEventArgs e)
        {

            Init();

            var floorId = 0;
            var name = txtName.Text;
            var nodesToRemove = new List<QRCodeNode>();
            try
            {
                floorId = Int32.Parse(txtFloor.Text);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
            if (floorId > 0)
            {
                nodesToRemove.AddRange(Nodes.Where(node => node.FloorId != floorId));
            }
            if (!string.IsNullOrWhiteSpace(name))
            {
                nodesToRemove.AddRange(Nodes.Where(node => node.DisplayName != name && node.RoomName != name));
            }
            foreach (var node in nodesToRemove)
            {
                Nodes.Remove(node);
            }
        }
    }
}
