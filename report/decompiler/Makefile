all:
	cd ../; ./setup-symlinks.sh
	pdflatex --shell-escape decompiler-report.tex
	bibtex decompiler-report
	pdflatex --shell-escape decompiler-report.tex
	pdflatex --shell-escape decompiler-report.tex

clean:
	find . -type l -delete
	rm -f *.log
	rm -rf _minted-*-report
	rm -f *-report.aux
	rm -f *-report.bbl
	rm -f *-report.blg
	rm -f *-report-blx.bib
	rm -f *-report.log
	rm -f *-report.out
	rm -f *-report.pdf
	rm -f *-report.run.xml
	rm -f *-report.toc
