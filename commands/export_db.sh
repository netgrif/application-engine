#!/usr/bin/env bash
echo "NAE database export:"
echo "dir name: [dump]"
read dir_name
if [[ ! ${dir_name} || ${dir_name} = *[^0-9]* ]]; then
    dir_name=dump
fi
mkdir ${dir_name}

echo "MySQL export:"
echo "user: "
read mysql_user
echo "database:"
read mysql_db
mysqldump -u ${mysql_user} -p ${mysql_db} > ${dir_name}/mysql.sql

echo "MongoDB export"
echo "database: "
read mongo_db
mongodump --db ${mongo_db} -o ${dir_name}

echo "Neo4j export"
echo "database: [graph.db]"
read neo4j_db
if [[ ! ${neo4j_db} || ${neo4j_db} = *[^0-9]* ]]; then
    neo4j_db=graph.db
fi

neo4j stop
neo4j-admin dump --database=${neo4j_db} --to=${dir_name}/neo4j.dump
neo4j start

zip -r dump-`date "+%F_%_H%_M"`.zip ${dir_name}