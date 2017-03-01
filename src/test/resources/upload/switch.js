    var search_opt = {
                source : "/search",
                minLength : 2,
                select : function(event, ui) {
                        window.location.replace(ui.item.id);
                }
        };