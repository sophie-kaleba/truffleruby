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

class Mono < Benchmarks
    def initialize()
        @a = ReceiverA.new(1,2)
        @b = ReceiverB.new(1,2)
        @c = ReceiverC.new(1,2)
        @d = ReceiverD.new(1,2)
        @e = ReceiverE.new(1,2)
        @f = ReceiverF.new(1,2)
        @g = ReceiverG.new(1,2)
        @h = ReceiverH.new(1,2)
        @i = ReceiverI.new(1,2)
        @j = ReceiverJ.new(1,2)

      @arr1 = [
        @a #will be last in the dispatch chain
      ]

      for i in 1..(1000 - @arr1.length()) do
        @arr1.push(@a)
      end

    end
  
    def benchmark
      [loop1(@arr1)]
    end
  
    def loop1(an_array)
      sum = 0
      an_array.each do |e|
        sum += e.get_x()
      end
      sum 
    end
    
    def verify_result(result)
      1000 === result[0]
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