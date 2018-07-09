var q = document.getSelection();
if (!q) {
  q = prompt('What to look up?', '');
}
if (q) {
  window.location = 'https://www.google.com/search?q=define:' + encodeURIComponent(q);
}
