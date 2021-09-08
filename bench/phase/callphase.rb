# truffleruby_primitives: true

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
require './bench/phase-micro/callbaseline'

class CallPhase < CallBaseline 
    def benchmark 
      arr1_aux = @arr1
      2.times do 
        # TODO - here annotate loop1
        loop1(@arr1)
        @arr1 = @arr2
        # TODO - here insert a phase switch
        Primitive.phase_enabled? 1,true
      end
      Primitive.phase_enabled? 0,true
      [1000, nil, 1000]
    end
  
    def loop1(an_array)
      sum = 0
      an_array.each do |p|
        sum += p.get_x()
      end
      sum 
    end
end