# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "TruffleRuby" do

  it "contains no other public methods" do
    (TruffleRuby.methods - Module.new.methods).sort.should == %i{
      full_memory_barrier graal? native? revision sulong?
    }.sort
  end

end