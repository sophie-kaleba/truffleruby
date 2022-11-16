class SameSymbolSameLine < Benchmarks
  def initialize()
    @arr1 = Array.new(10)

    @a = ReceiverA.new("foo", "bar")
    @arr1.fill(@a)
  end

  def benchmark
    sum = 0
    sum = @arr1[0].get_x().concat(@arr1[5].get_x())
    sum
  end
  
  def verify_result(result)
    true
  end
end

class ReceiverA 
  def initialize(x,y)
    @x = x
    @y = y
  end

  def get_x() 
    @x
  end

  def get_y() 
    @y
  end
end

class ReceiverB < ReceiverA
  def initialize(x,y)
      @x = x
      @y = y
    end
  
    def get_x() 
      1 + 1
    end
end

class ReceiverC < ReceiverA
  def initialize(x,y)
      @x = x
      @y = y
    end
  
    def get_x() 
      1 + 2
    end
end

class ReceiverD < ReceiverA
  def initialize(x,y)
      @x = x
      @y = y
    end
  
    def get_x() 
      1 + 3
    end
end

class ReceiverE < ReceiverA
  def initialize(x,y)
      @x = x
      @y = y
    end
  
    def get_x() 
      1 + 4
    end
end

class ReceiverF < ReceiverA
  def initialize(x,y)
      @x = x
      @y = y
    end
  
    def get_x() 
      1 + 5
    end
end

class ReceiverG < ReceiverA
  def initialize(x,y)
      @x = x
      @y = y
    end
  
    def get_x() 
      1 + 6
    end
end

class ReceiverH < ReceiverA
  def initialize(x,y)
      @x = x
      @y = y
    end
  
    def get_x() 
      1 + 7
    end
end

class ReceiverI < ReceiverA
  def initialize(x,y)
      @x = x
      @y = y
    end
  
    def get_x() 
      1 + 8
    end
end

class ReceiverJ < ReceiverA
  def initialize(x,y)
      @x = x
      @y = y
    end
  
    def get_x() 
      1 + 9
    end
end