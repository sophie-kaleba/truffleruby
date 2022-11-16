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

class Slambdamega < Benchmarks
  def initialize()
    @arr1 = Array.new(1000)

    @block = lambda { 61 }
    @arr1.fill(@block)
  
    @arr1[1] = lambda { 62 }
    @arr1[2] = lambda { 63 }
    @arr1[3] = lambda { 64 }
    @arr1[4] = lambda { 65 }
    @arr1[5] = lambda { 66 }
    @arr1[6] = lambda { 67 }
    @arr1[7] = lambda { 68 }
    @arr1[8] = lambda { 69 }
  end

  def benchmark
    loop1(@arr1)
  end

  def loop1(an_array)
    sum = 0
    an_array.each do |f|
      sum += f.call() + 2
    end
    sum 
  end
  
  def verify_result(result)
    63036 === result
  end
end