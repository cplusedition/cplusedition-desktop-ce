if [ ! -e ./configure.ac ];  then
        echo "ERROR: ./configure.ac not found, abort";
        exit 1;
fi
rm -f ./configure
scriptsdir=$(dirname $0)
echo "### autoreconf ..."
time autoreconf -fi
sleep 1
echo "### configure ..."
time ./configure --prefix=$(pwd)/dist
exec ${scriptsdir}/rebuild.sh
