var StringUtils = {
	addDoubleSlash: function (text) {
		return "// " + text;
	},
	escapeHTML: function (text) {
		var fakeDiv = $("<div/>", {
			text: text
		});
		
		return fakeDiv.html();
	},
	join: function (delimiter, ...text) {
		return $.grep(text, Boolean).join(delimiter);
	},
	replaceDotByMinus: function (text) {
		return text.replace(/\./g, "-");
	},
	unescapeHTML: function (text) {
		var unescapedHTML = $.parseHTML(text);
		return unescapedHTML ? unescapedHTML[0].nodeValue : "";
	}
};