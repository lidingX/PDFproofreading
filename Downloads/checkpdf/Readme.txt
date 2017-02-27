@ author:lidingxu@pku.edu.cn
@ checkpdf-2.0.0: requires jdk 1.8 at least.
@ functions: 1¡¢regex search engine, search a regex and highlight the match chunks.
	     2¡¢proofread a text, use embbeded languagetool rules and external rules.
	     3¡¢proofread a pdf, use use embbeded languagetool rules and external rules, and
highlight rulematches.
  options:  it's impossible to recogonize every words especially names, so you can close spelling check,
or close spelling check of upperclass words.
  io: read pdf in ./pdf file, output result in ./result file.
