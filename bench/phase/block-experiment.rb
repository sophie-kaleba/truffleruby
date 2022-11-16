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
 
class BlockExperiment < Benchmarks
    def benchmark
        result = 0
        #a = Proc.new {result += 1} # Proc class constructor
        #b = proc {result += 2} # Kernel proc method
        # the c block will be created on the spot later to use yield

       # d = lambda {result += 4}
       # e = ->() { result += 5 }
       # f = lambda {result += 6}

        #arr = [a, b]
        array = [1, 2]
        array.each { |x| result += x }
        #array.each { |x| result += x }
        #for el in arr do
        #  el.call()
        #end
        # There are 3 ways to activate a closure in Ruby
        
        #add_once {result += 3}

        # also 3 ways to activate a lambda
        #d.call
        #e.()
        #f[]
        
        result
    end

    #def add_once
    #  yield 
    #end
   
    def verify_result(result)
      42 == result
    end
  end
  
 
  

