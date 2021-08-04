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

class Sproxybase < Benchmark
    def initialize()
      @p = Point.new(1,1)
      @arr1 = [
        @p, #will be last in the dispatch chain
        ProxyA.new(@p),
        ProxyB.new(@p),
        ProxyC.new(@p),
        ProxyD.new(@p),
        ProxyE.new(@p),
        ProxyF.new(@p),
        ProxyG.new(@p)
      ]
  
      @arr2 = [@p, @p, @p, @p, @p, @p, @p, @p]
  
      for i in 1..992 do
        @arr1.push(@p)
        @arr2.push(@p)
      end
    end
  
    def benchmark
      [loop1(@arr1), loop2(@arr2)]
    end
  
    def loop1(an_array)
      sum = 0
      an_array.each do |p|
        sum += p.get_x()
      end
      sum 
    end
  
    def loop2(an_array)
      sum = 0
      an_array.each do |p|
        sum += p.get_x()
      end
      sum
    end
    
    def verify_result(result)
      1000 === result[0] && 1000 === result[1]
    end
  end
  
  class Point 
    def initialize(x,y)
      @x = x
      @y = y
    end
  
    def get_x() 
      @x
    end
  end
  
  class ProxyA 
    def initialize(p)
      @p = p
    end
  
    def get_x() 
      @p.get_x()
    end
  end
  
  class ProxyB 
    def initialize(p)
      @p = p
    end
  
    def get_x() 
      @p.get_x()
    end
  end
  
  class ProxyC 
    def initialize(p)
      @p = p
    end
  
    def get_x() 
      @p.get_x()
    end
  end
  
  class ProxyD 
    def initialize(p)
      @p = p
    end
  
    def get_x() 
      @p.get_x()
    end
  end
  
  class ProxyE 
    def initialize(p)
      @p = p
    end
  
    def get_x() 
      @p.get_x()
    end
  end
  
  
  class ProxyE 
    def initialize(p)
      @p = p
    end
  
    def get_x() 
      @p.get_x()
    end
  end
  
  class ProxyF
    def initialize(p)
      @p = p
    end
  
    def get_x() 
      @p.get_x()
    end
  end
  
  class ProxyG 
    def initialize(p)
      @p = p
    end
  
    def get_x() 
      @p.get_x()
    end
  end