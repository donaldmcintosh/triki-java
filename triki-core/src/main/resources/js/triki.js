function initEditor() {
	// Add 4 to count - only need to add two, but keep it safe
    rowcount += 4;

    var search_opt = {
                source : "/search",
                minLength : 2,
                select : function(event, ui) {
                        window.location.replace(ui.item.id);
                }
        };

        var lookup_opt = {
                source : "/search",
                minLength : 2,
                select : function(event, ui) {
                   this.value = ui.item.value
                   var n = this.name.indexOf(':');
                   var base = this.name.substring(0, n != -1 ? n : this.name.length);
                   this.name = base + ":" + ui.item.encodeurl
                }
        };
        
        var lookup_prop = {
                source : "/search/properties",
                minLength : 2,
                select : function(event, ui) {
                   this.value = ui.item.value
                   var n = this.name.indexOf(':');
                   var base = this.name.substring(0, n != -1 ? n : this.name.length);
                   this.name = base + ":" + ui.item.encodeurl
                }
        };
        
        var lookup_prefix = {
                source : "/search/prefixes",
                minLength : 2,
                select : function(event, ui) {
                   this.name = "prefix:" + ui.item.encodeurl
                   this.value = ui.item.value
                }
        };

        var minus_opt = function() {
            $(this).closest('.graphrow').remove();
            return false;
        };

        $("#search").autocomplete(search_opt);
        $(".lookup").autocomplete(lookup_opt);
        $(".lookupprop").autocomplete(lookup_prop);
        $(".lookupprefix").autocomplete(lookup_prefix);
        $('.minus').click(minus_opt);


    $(".addtext").click(function() {
        var newrow = $('<div class="graphrow"><div class="addprop"><input name="proptext' + rowcount + '" class="lookupprop" size="30"/></div><div class="addobj"><textarea name="obj'+ rowcount + '" rows="3" cols="38"></textarea><a class="minus"><img src="/content/minus_15x15.png"></a></div></div>');
        $('.lookupprop', newrow).autocomplete(lookup_prop);
        $('.minus', newrow).click(minus_opt);
        $('.graphrow:last').after(newrow);
        rowcount++;
        return false;
     });
    
    $(".addlink").click(function() {
        var newrow = $('<div class="graphrow"><div class="addprop"><input name="proplink' + rowcount + '" class="lookupprop" size="30"/></div><div class="addobj"><input name="objlink' + rowcount + '" class="lookup" size="50" /><a class="minus"><img src="/content/minus_15x15.png"></a></div></div>');
        $('.lookupprop', newrow).autocomplete(lookup_prop);
        $('.lookup', newrow).autocomplete(lookup_opt);
        $('.minus', newrow).click(minus_opt);
        $('.graphrow:last').after(newrow);
        rowcount++;
        return false;
     });

    $(".addtextfile").click(function() {
        var newrow = $('<div class="graphrow"><div class="addcontent"><textarea name="obj'+ rowcount + '" rows="20" cols="100"></textarea><a class="minus"><img src="/content/minus_15x15.png"></a></div></div>');
        $('.minus', newrow).click(minus_opt);
        $('.graphrow:last').after(newrow);
        rowcount++;
        return false;
     });
    
    $(".addbinfile").click(function() {
        var newrow = $('<div class="graphrow"><div class="addcontent"><input name="obj' + rowcount + '" type="file" multiple="multiple"/><a class="minus"><img src="/content/minus_15x15.png"></a></div></div>');
        $('.minus', newrow).click(minus_opt);
        $('.graphrow:last').after(newrow);
        rowcount++;
        return false;
     });
    

     $("#validate").click(function(e){
        e.preventDefault();
        $.ajax({type: "POST",
              url: "validate",
              data: {
                  template: $("#content").val()
              },
              error:function(result){
                  $("#templatemsg").html(result.statusText);
              },
              success:function(result){
                  $("#templatemsg").html(result.statusText);
              }});
     });
     
     $('#content').each(function(){
    	 $.get(contentPath, function(data){
    		 var textarea = $('#content')[0]
    		 textarea.textContent = data;
    	 }, "text");
     });
     
};

window.onload = function() {
	initEditor()
}
