var Admin = {

    mapId: 0,
    floorId: 0,

    loadMaps: function() {
        $('.tab').removeClass('active').removeClass("selectable");
        $('#mapsTab').addClass('active');
        $("body").addClass("loading");
        $('#floorsTab').off("click");
        $('#mapsTab').off("click");
        $.ajax({
            url: window.basePath + "Admin/GetMaps",
            success: function(result) {
                $('#adminContent').html(result);
                $("body").removeClass("loading");
            }
        });
    },

    loadFloors: function (mapId) {

        if (mapId === null)
            return;
        
        if (typeof mapId === "object") {
                this.mapId = mapId.data.mapId;
        } else {
            this.mapId = mapId;
        }

        $('.tab').removeClass('active');
        $('#floorsTab').addClass('active');
        $('#mapsTab').addClass('selectable');
        $('#mapsTab').on("click", this.loadMaps);
        $("body").addClass("loading");
        $.ajax({
            url: window.basePath + "Admin/GetFloorsForMap/" + this.mapId,
            success: function (result) {
                $('#adminContent').html(result);
                $("body").removeClass("loading");
            }
        });
    },
    
    deleteMap: function(mapId) {
        if (mapId === null)
            return;

        var id = mapId;
        
        $('#DeleteDialog').html("Soll die Map " + mapId + " gelöscht werden?");
        $('#DeleteDialog').dialog({
            dialogClass: "no-close",
            modal: true,
            title: "Map löschen",
            appendTo: "#body",
            buttons: {
                Löschen: function () {
                    $("body").addClass("loading");
                    $.ajax({
                        url: window.basePath + "Admin/DeleteMap/" + id,
                        success: function (result) {
                            $('#adminContent').html(result);
                            $("body").removeClass("loading");
                        }
                    });
                    $(this).dialog("close");
                },
                Abbrechen: function () {
                    $(this).dialog("close");
                }
            }
        });

        
    },

    loadFloorplan: function (mapId, floorId) {
        
        if (mapId == null || floorId == null)
            return;

        if (typeof mapId === "object") {
            this.mapId = mapId.data.mapId;
            this.floorId = mapId.data.floorId;
        } else {
            this.mapId = mapId;
            this.floorId = floorId;
        }

        $('.tab').removeClass('active');
        $('#layerTab').addClass('active');
        $('#floorsTab').addClass('selectable');
        $('#floorsTab').on("click", { mapId: mapId }, this.loadFloors);
        $("body").addClass("loading");
        $.ajax({
            url: window.basePath + "Admin/GetFloor/" + this.floorId,
            success: function (result) {
                $('#adminContent').html(result);
                $("body").removeClass("loading");
            }
        });
    },
    
    deleteFloorplan: function(mapId, floorId) {
        if (mapId == null || floorId == null)
            return;

        var fid = floorId;
        var mid = mapId;

        $('#DeleteDialog').html("Soll der Floor " + floorId + " gelöscht werden?");
        $('#DeleteDialog').dialog({
            dialogClass: "no-close",
            modal: true,
            title: "Floor löschen",
            appendTo: "#body",
            buttons: {
                Löschen: function () {
                    $("body").addClass("loading");
                    $.ajax({
                        url: window.basePath + "Admin/DeleteFloor/" + mid + "/" + fid,
                        success: function (result) {
                            $('#adminContent').html(result);
                            $("body").removeClass("loading");
                        }
                    });
                    $(this).dialog("close");
                },
                Abbrechen: function () {
                    $(this).dialog("close");
                }
            }
        });
        
    }
};