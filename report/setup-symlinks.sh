for f in shared/*.tex shared/*.sty shared/*.bib shared/technical-background/*.tex shared/*.tikz
do
    filename=$(basename $f)
    prefix="shared/"
    destination=${f#$prefix}
    echo "Processing $filename file..."
    echo "Creating link to ../$f at decompiler/$destination"
    echo "Creating link to ../$f at simulator/$destination"

    
    ln -s --force "../$f" "decompiler/$destination" > /dev/null 2>&1
    ln -s --force "../$f" "simulator/$destination"  > /dev/null 2>&1
done

# Let's add our symlinks to the .gitignore file. 
find -type l | cut -c 2- >> .gitignore

# Remove duplicate entries in the .gitignore file
awk '!a[$0]++' .gitignore
