import sys
import os

fin = sys.argv[1]
commit = fin.split("-")[1].strip()[:-4]

parent_dir = os.getcwd()
path = os.path.join(parent_dir, commit)
os.mkdir(path)

split_targets = commit+"_truffle_split_statistics.csv"
split_fout = open(os.path.join(path,split_targets), 'wt')

def parse_one_benchmark(fp, line):
    total_split_count = line.split(":")
    unforced_split_count = fp.readline().split(":")
    node_created_splitting = fp.readline().split(":")
    node_created_nosplitting = fp.readline().split(":")
    node_created_absolute = fp.readline().split(":")
    bench = fp.readline().split("_") #[FROMBENCH]%(benchmark)s-%(suite)s-%(executor)s'

    split_fout.write(bench[1].strip()+","+bench[2].strip()+","+bench[3].strip()+","+commit+","+total_split_count[1].strip()+","+unforced_split_count[1].strip()+","+node_created_splitting[1].strip()+","+node_created_nosplitting[1].strip()+","+node_created_absolute[1].strip()+"\n")
    
with open(fin) as fp:
    split_fout.write("benchmark,suite,executor,commit,total_split_count,unforced_split_count,node_created_splitting,node_created_nosplitting,node_created_absolute\n")
    while True:
        line = fp.readline()
        if "TotalSplitCount" in line:
            parse_one_benchmark(fp, line)
        if not line:
            break
    split_fout.close()

os.rename(os.path.join(parent_dir, fin), os.path.join(path,fin) )
