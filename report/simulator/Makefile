all:
	cd ../; ./setup-symlinks.sh
	pdflatex --shell-escape simulator-report.tex
	bibtex simulator-report
	pdflatex --shell-escape simulator-report.tex
	pdflatex --shell-escape simulator-report.tex

clean:
	find . -type l -delete
	rm -f *.log
	rm -rf _minted-simulator-report
	rm -f simulator-report.aux
	rm -f simulator-report.bbl
	rm -f simulator-report.blg
	rm -f simulator-report-blx.bib
	rm -f simulator-report.log
	rm -f simulator-report.out
	rm -f simulator-report.pdf
	rm -f simulator-report.run.xml
	rm -f simulator-report.toc

