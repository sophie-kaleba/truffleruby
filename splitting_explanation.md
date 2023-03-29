########### START OF FIRST ITERATION
first call to engage_pets -> foomain -> shout happens
High Meow5 

second call to engage_pets -> foomain -> shout happens. shout cache turns polymorphic
A flag check is performed
[engine] [poly-event] Polymorphic event! Source: LookupMethodNodeAux@2d272b0d                RegularRecursiveSplitting#foomain
[engine] [poly-event] One caller! Analysing parent.                                          RegularRecursiveSplitting#foomain
[engine] [poly-event]   Monomorphic caches! Set needs split to false                         RegularRecursiveSplitting#engage_pets
[engine] [poly-event]   Return: false                                                        RegularRecursiveSplitting#engage_pets
[engine] [poly-event] Set needs split to true via parent                                     RegularRecursiveSplitting#foomain
[engine] [poly-event] Return: true                                                           RegularRecursiveSplitting#foomain
High Woof43

The flag is properly set as soon as the new class has been uncovered in the LookupMethodNode, so when the cache turns polymorphic in CallInternalMethodNode, it is ignored
[engine] [poly-event] Polymorphic event! Source: CallInternalMethodNodeAux@fb713e7           RegularRecursiveSplitting#foomain
[engine] [poly-event] Early return: true callCount: 2, numberOfKnownCallNodes: 1             RegularRecursiveSplitting#foomain
########### END OF FIRST ITERATION

########### START OF SECOND ITERATION
The original engage_pets call node gets processed. We split foomain just before it is called, so the split, uninitliased version of foomain gets executed.
This version has a brand new empty cache, that will get populated by one (cat) entry after the first call to foomain
[engine] split 148-1b5c3e5f-3     RegularRecursiveSplitting#foomain                           |AST   18|Tier 1|Calls/Thres       2/    1|CallsAndLoop/Thres       2/  400|SourceSection RegularRecursiveSplitting.rb~24:443-530
High Meow1

second call to engage_pets -> foomain<split> -> shout happens. shout cache turns polymorphic, with a new (dog) entry.
This triggers a new flag checking, first on the split version of foomain, and also on its parents engage_pets

[engine] [poly-event] Polymorphic event! Source: LookupMethodNodeAux@13741d5a                RegularRecursiveSplitting#foomain <split-1475>
[engine] [poly-event] One caller! Analysing parent.                                          RegularRecursiveSplitting#foomain <split-1475>
[engine] [poly-event]   Monomorphic caches! Set needs split to false                         RegularRecursiveSplitting#engage_pets
[engine] [poly-event]   Return: false                                                        RegularRecursiveSplitting#engage_pets
[engine] [poly-event] Set needs split to true via parent                                     RegularRecursiveSplitting#foomain <split-1475>
[engine] [poly-event] Return: true                                                           RegularRecursiveSplitting#foomain <split-1475>
High Woof90

The flag is properly set as soon as the new class has been uncovered in the LookupMethodNode, so when the cache turns polymorphic in CallInternalMethodNode, it is ignored
[engine] [poly-event] Polymorphic event! Source: CallInternalMethodNodeAux@6b69761b          RegularRecursiveSplitting#foomain <split-1475>
[engine] [poly-event] Early return: true callCount: 2, numberOfKnownCallNodes: 1             RegularRecursiveSplitting#foomain <split-1475>
########### END OF SECOND ITERATION

No new polymorphic event (even though the cache stays polymorphic), so no flag checks happens
High Meow9
High Woof98
