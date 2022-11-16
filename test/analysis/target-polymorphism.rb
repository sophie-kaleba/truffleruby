# This code is derived from the SOM benchmarks, see AUTHORS.md file.
#
# Copyright (c) 2015-2016 Stefan Marr <git@stefan-marr.de>
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the 'Software'), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.

class TargetPolymorphism < Benchmarks
    def initialize
        @a = ReceiverA.new(1,2)
        @b = ReceiverB.new(1,2)
        @c = ReceiverC.new(1,2)
        @d = ReceiverD.new(@a)

       @arr1 = [@a, @b, @c, @d]
    end

  
    def benchmark
      [loop1(@arr1)]
    end
  
    def loop1(an_array)
      sum = 0
      an_array.each do |e|
        sum += e.get_x()
        #Primitive.phase_enabled? 1,false
      end
      sum 
    end

    def verify_result(result)
      result[0] == 4
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
  end
  
  class ReceiverB < ReceiverA
    def initialize(x,y)
        @x = x
        @y = y
      end
  end

  class ReceiverC < ReceiverA
    def initialize(x,y)
        @x = x
        @y = y
      end
    
    def get_x() 
      super
    end
  end

  class ReceiverD < ReceiverA
    def initialize(p)
        @p = p
      end
    
      def get_x() 
        @p.get_x()
      end
  end