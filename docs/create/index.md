---
layout: create
title: Create a Lite App
description: Create a Lite App with Hermit
---

<article class="width-limit content-background">
  <section class="centered">
    <h2>Create a Lite App</h2>
    <a id="create-link" class="create-ui">
      <img id="create-icon" src="{{ site.cdn_url }}/favicon.png">
      <p><span id="create-name"></span></p>
    </a>
    <p>Tap on the icon above to create a Lite App</p>
  </section>
</article>

<article class="background-1">
  <section class="width-limit centered">
    <h2>Don’t have <span class="notranslate">Hermit</span> installed?</h2>
    <p class="tagline">You’ll need it to start creating Lite Apps.</p>
    <a class="download-button pill-button" href="https://play.google.com/store/apps/details?id=com.chimbori.hermitcrab" target="_blank">get it now</a>
  </section>
</article>

<article class="width-limit content-background">
  <section class="centered">
    <h3>Viewing in another app?</h3>
    <p>Try opening this page in your browser.</p>
  </section>
</article>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js"></script>
<script>
  function getQueryVariable(paramName) {
    var query = window.location.search.substring(1);
    var vars = query.split('&');
    for (var i = 0; i < vars.length; i++) {
      var pair = vars[i].split('=');
      if (pair[0] === paramName) {
        return decodeURIComponent(pair[1]);
      }
    }
    return '';
  }

  $(document).ready(function() {
    // If we use the same "https://" scheme as the original link, then Chrome won’t open Hermit
    // to handle the link if the user had reached the page by manually typing the address. So,
    // to force Hermit to open the link, change scheme to one that Chrome can’t handle, and only
    // Hermit can handle.
    var createUrl = window.location.toString().replace(/http[s]{0,1}\:\/\//, 'hermit://');

    $('#create-link').attr('href', createUrl);
    var urlParam = getQueryVariable('url');
    if (urlParam.indexOf('http://') == -1 && urlParam.indexOf('https://') == -1) {
      urlParam = 'https://' + urlParam;
    }
    var url = new URL(urlParam);

    // Show the name of the Lite App, if available, or else show the hostname.
    // Names are available for all Lite Apps linked from the Hermit Library.
    // Lite Apps linked from elsewhere may or may not have the name parameter set.
    var name = getQueryVariable('name');
    $('#create-name').text(name != '' ? name : url.hostname);

    // If the "icon" parameter is present, then use it, else the ClearBit API.
    var iconParam = getQueryVariable('icon');
    var iconSrc = iconParam != '' ? iconParam : 'https://logo.clearbit.com/' + url.hostname;
    $('#create-icon').attr('src', iconSrc);
  });
</script>
