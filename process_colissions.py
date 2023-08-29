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
        if match != "":
            calls += 1
            match = match.group(1).split(",")
            signature_hash = match[0]
            if signature_hash != "-2":
                arguments = match[1:len(match)-1]
            
                if str(arguments) not in all_signatures.keys():
                    all_signatures[str(arguments)] = signature_hash
                else:
                    current_signature = all_signatures[str(arguments)]
                    if current_signature != signature_hash:
                        collisions+=1
                        if str(arguments) not in all_collisions.keys():
                            all_collisions[str(arguments)] = 1
                        else:
                            all_collisions[str(arguments)] = all_collisions[str(arguments)] +1
                        #print("[COLLISION] "+current_signature+" "+ str(arguments))
