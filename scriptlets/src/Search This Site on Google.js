var query = prompt('Search for:', '');
if (query) {
  window.location = 'https://www.google.com/search?q=' + encodeURIComponent(query) + '%20site:' + window.location.hostname;
}