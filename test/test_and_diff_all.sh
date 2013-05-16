rm *.ref.out -f
rm *.ref.errors -f
FILES="./*.cl"
for f in $FILES
do
    echo "reference semant: '$f'"
    /usr/class/cs143/bin/coolc -k "$f" > "$f.ref.errors" 2>&1
    rm "lexer.out" -f
    rm "parser.out" -f
    mv "semant.out" "$f.ref.out"
    echo "my-semant: '$f'"
    ../mysemant "$f" > "$f.my.errors" 2>&1
    rm "lexer.out" -f
    rm "parser.out" -f
    mv "semant.out" "$f.my.out"
done
for f in $FILES
do
    diff "$f.ref.out" "$f.my.out" > out.tmpdiff
    diff "$f.ref.errors" "$f.my.errors" > errors.tmpdiff
    if[[ -s out.tmpdiff] -a [ -s errors.tmpdiff]]
    then
	echo "'$f' passes!"
    else
	echo "begin AST diff: '$f' (ours on right)"
	cat out.tmpdiff
	echo "end AST diff"
	echo "begin errors diff: '$f' (ours on right)"
	cat errors.tmpdiff
	echo "end errors diff"
    fi
    rm *.tmpdiff -f
done
rm *.s -f