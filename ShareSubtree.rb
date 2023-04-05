class ShareSubtree

  class Cat
    def shout(pitch)
      a = 1 + Random.new.rand(10)
      puts pitch+" Meow"+a.to_s
    end 
  end

  class Dog 
    def shout(pitch)
      a = 1 + Random.new.rand(100)
      puts pitch+" Woof"+a.to_s
    end 
  end

  def outer()
    # those 2 call-sites are always called on the same argument ~ don't split!
    middle(Cat.new)
    middle(Dog.new)
  end

  def middle(pet)
    inner(pet) #foomain has to be split so the cache of the shout call-site is cleaned
  end

  def inner(pet)
    pet.shout("High") #Call node for this call is part of mainCT#rootNode#SequenceRootNode.body#RubyNode.body#CatchBreakNode.body#loopNode#repeatingNode.body#SequenceNode.body#RubyCallNode
  end
end

b = ShareSubtree.new()

for i in 1..3 do
  b.outer
  # 1st iterationm middle and outer flagged as needsSplit
  # 2d iteration, middle and outer are split
  # 3rd iteration no split or split flagging happens
end

b.middle(Cat.new) #will trigger a full splitting again, but we could dispatch to common subtree instead

# what happens if I 
# for i in 1..3 do
#   b.inner(Cat::new)
# end

# if I do this now, middle is not flagged as a dispatch location, so a normal split will occur
# we might want to be smarter with this (ie maybe split per target and not per subtree?)
