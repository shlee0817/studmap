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

        if (mapId == null)
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
    }
};