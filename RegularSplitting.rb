class RegularSplitting

  def foomain(index)
    i = 0;
    a = [Cat.new, Dog.new]
    while i < index
      e = a[i]
      e.shout("High") #Call node for this call is part of mainCT#rootNode#SequenceRootNode.body#RubyNode.body#CatchBreakNode.body#loopNode#repeatingNode.body#SequenceNode.body#RubyCallNode
      i = i + 1
    end 
  end
end

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

b = RegularSplitting.new()

b.foomain(1)
b.foomain(2) #the split flag has been set to true
b.foomain(1) #need this extra call to trigger splitting
