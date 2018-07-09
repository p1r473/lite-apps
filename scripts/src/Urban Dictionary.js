var q = document.getSelection();
if (!q) {
  q = prompt('What to look up?', '');
}
if (q) {
  window.location = 'https://www.urbandictionary.com/define.php?term=' + encodeURIComponent(q);
}
