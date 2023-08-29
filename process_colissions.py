import re
import sys

fin = sys.argv[1]

line_rgx = r'^.*: (.*)'
all_signatures = dict()
all_collisions = dict()
arguments = []
collisions = 0
calls = 0

with open(fin) as fp:
    while True:
        line = fp.readline()

        if line == "":
            print("[TOTAL COLLISIONS]: "+str(collisions)+" for "+str(calls)+" calls. "+str((collisions/calls)*100)+"%")
            for key in all_collisions:
                print(key, "->", all_collisions[key])
            break

        match = re.search(line_rgx, line)
        if match != "" and match.group(1) != None:
            calls += 1
            
            match = match.group(1).split("|")

            if len(match) == 2:
                arguments = match[0].split(",")
                hashes = match[1].split(",")
            
                if str(arguments) not in all_signatures.keys():
                    all_signatures[str(arguments)] = str(hashes)
                else:
                    current_hashes = all_signatures[str(arguments)]
                    if current_hashes != str(hashes):
                        collisions+=1
                        if str(arguments) not in all_collisions.keys():
                            all_collisions[str(arguments)] = {str(hashes)}#1 #[1, str(hashes)]
                        else:
                            all_collisions[str(arguments)].add(str(hashes)) #= all_collisions[str(arguments)] +1 #[all_collisions[str(arguments)][0] +1, all_collisions[str(arguments)][1]+"~"+str(hashes)]
                        #print("[COLLISION] "+current_signature+" "+ str(arguments))
