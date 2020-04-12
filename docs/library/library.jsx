---
---

var libraryJson = {};

var CURRENT_VERSION_CODE = 130000;

function makeCreateUrl(liteApp) {
  var createUrl = 'https://hermit.chimbori.com/create';
  createUrl += '?url=' + encodeURIComponent(liteApp.url);
  createUrl += '&name=' + encodeURIComponent(liteApp.name);  // The /create page uses this; don’t change.
  createUrl += '&icon=' + encodeURIComponent(liteApp.image_url);  // The /create page uses this; don’t change.
  createUrl += '&app=' + encodeURIComponent(liteApp.manifest_url);  // The app uses this; don’t change.
  return createUrl;
}

function LiteApp(props) {
  return <div className="lite-app-icon">
    <a href={ makeCreateUrl(props.liteApp) }>
      <img className="lite-app-icon-image" src={ props.liteApp.image_url }/>
      <span className="lite-app-icon-name">{ props.liteApp.name }</span>
    </a>
  </div>
}

function isDisplayed(liteApp) {
  return !liteApp.hasOwnProperty('display') || liteApp.display;
}

function Category(props) {
  return <div>
    <h3 className="lite-app-category-count">{props.category.apps.length} Lite Apps</h3>
    <h2>{ props.category.category.name }</h2>
    <div className="lite-app-category">
    {
      props.category.apps.map(function(liteApp, i) {
        if (!isDisplayed(liteApp)) {
          return null;
        }
        return <LiteApp liteApp={ liteApp } key={ i }/>;
      })
    }
    </div>
  </div>
}

function Library(props) {
  return <div> {
    props.library.categories.map(function(category, i) {
      var isAtleastOneLiteAppDisplayed = false;
      for (var liteAppIndex in category.apps) {
        if (isDisplayed(category.apps[liteAppIndex])) {
          isAtleastOneLiteAppDisplayed = true;
          break;
        }
      }

      return isAtleastOneLiteAppDisplayed
          ? <Category category={ category } key={ i }/>
          : null;
    })
  } </div>
}


function updateDisplay(libraryJson) {
  ReactDOM.render(
    <Library library={ libraryJson }/>,
    document.querySelector('.lite-apps-json')
  );
}

function applyQueryFilter(queryText) {
  var isQueryBlank = queryText.length == 0;
  for (var categoryIndex in libraryJson.categories) {
    var category = libraryJson.categories[categoryIndex];
    for (var liteAppIndex in category.apps) {
      var liteApp = category.apps[liteAppIndex];
      liteApp.display = isQueryBlank;
      if (liteApp.name.toLowerCase().indexOf(queryText) != -1 ||
          liteApp.url.toLowerCase().indexOf(queryText) != -1) {
        liteApp.display = true;
      }
    }
  }
}

document.querySelector('#query').addEventListener('input', function(e) {
  applyQueryFilter(e.target.value.toLowerCase());
  updateDisplay(libraryJson);
});

function getQueryVariable(paramName) {
  var query = window.location.search.substring(1);
  var vars = query.split('&');
  for (var i = 0; i < vars.length; i++) {
    var pair = vars[i].split('=');
    if (pair[0] === paramName) {
      return pair[1];
    }
  }
  return '';
}

function fetchJson() {
  $.getJSON("../lite-apps/lite-apps.json", function (data) {
    libraryJson = data;
    applyQueryFilter(document.querySelector('#query').value.toLowerCase());
    updateDisplay(libraryJson);
  });
}

function showMessageIfVersionTooOld() {
  var versionParam = getQueryVariable('v');
  if (versionParam == '') {
    return;
  }
  var parsedVersion = versionParam.match(/([0-9]+)\.([0-9]+)\.([0-9]+)/);
  var versionCode = parseInt(parsedVersion[1], 10) * 10000
      + parseInt(parsedVersion[2], 10) * 100
      + parseInt(parsedVersion[3], 10);
  if (versionCode < CURRENT_VERSION_CODE) {
    document.querySelector('.update-required').style.display = 'block';
  }
}

function showQueryParamInSearchBox() {
  var queryParam = getQueryVariable('q');
  var queryText = queryParam;
  if (queryParam != null &&
      queryParam != undefined &&
      queryParam.length != 0 &&
      queryParam.indexOf('http') == 0) {  // Only if query is (likely to be) a URL, try to parse a hostname from it.
    var queryAsUrl = document.createElement('a');
    queryAsUrl.href = queryParam;
    if (queryAsUrl != null) {
      queryText = queryAsUrl.hostname;
    }
  }
  document.querySelector('#query').value = queryText;
  document.querySelector('#query').focus();
}

showMessageIfVersionTooOld();
showQueryParamInSearchBox();
fetchJson();
