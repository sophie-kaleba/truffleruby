class Misprediction

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

  def initialize()
    @a = Array.new(2)
    @a = [Misprediction::Cat.new, Misprediction::Dog.new]  
  end

  def outer()
    # those 2 call-sites are always called on the same argument ~ don't split!
    middle(0)
    middle(1)
  end

  def middle(pet_idx)
    inner(pet_idx) #foomain has to be split so the cache of the shout call-site is cleaned
  end

  def inner(pet_idx)
    pet_object = @a[pet_idx] 
    pet_object.shout("High") 
  end
end

m = Misprediction.new()

# in this example, there is only a unique contextSignature, as we always rely on integers
for i in 1..2 do
  m.outer
  # 1st iterationm middle and outer flagged as needsSplit
  # 2d iteration, middle and inner are split, after the last call to Dog, there is a polymorphic event and the split targets middle and inner are flagged as invalid 
end

m.middle(0)
#for i in 1..3 do
  # before the first iteration, try to dispatch to subtree, but subtree is invalid. Instead of executing the invalid targets, we bind m.middle to the original target, delete the pair (ctx, subtree root) in the map, split again, and add the new pair in the map.
#  m.middle(0)
#end

# This should yield:
# split - middle
# split - inner
# mispredict - inner 
# mispredict - outer 
# split - middle 
# split - inner 
