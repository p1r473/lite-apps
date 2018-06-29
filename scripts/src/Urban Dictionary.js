var q = document.getSelection();
if (!q) {
  q = prompt('What to look up?', '');
}
if (q) {
  window.location = 'http://www.urbandictionary.com/define.php?term=' + encodeURIComponent(q);
}
