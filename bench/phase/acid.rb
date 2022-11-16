# Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

class Acid < Benchmarks 
  def initialize
    @bar = Bar.new
  end

  def benchmark 
    @bar.foo(14, 8, 6)
  end

  def verify_result(result)
    result == 22
  end

  module Foo
    def self.foo(a, b, c)
      hash = {a: a, b: b, c: c}
      array = hash.map { |k, v| v }
      x = array[0]
      y = [a, b, c].sort[1]
      x + y
    end
  end

  class Bar 
    def method_missing(method, *args)
      if Foo.respond_to?(method)
        Foo.send(method, *args)
      else
        0
      end
    end
  end
end





