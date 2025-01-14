require_relative '../../enumerable/shared/enumeratorized'
require_relative '../shared/iterable_and_tolerating_size_increasing'

describe :keep_if, shared: true do
  it "deletes elements for which the block returns a false value" do
    array = [1, 2, 3, 4, 5]
    array.send(@method) {|item| item > 3 }.should equal(array)
    array.should == [4, 5]
  end

  it "returns an enumerator if no block is given" do
    [1, 2, 3].send(@method).should be_an_instance_of(Enumerator)
  end

  it "updates the receiver after all blocks" do
    a = [1, 2, 3]
    a.send(@method) do |e|
      a.length.should == 3
      false
    end
    a.length.should == 0
  end

  before :all do
    @object = [1,2,3]
  end
  it_should_behave_like :enumeratorized_with_origin_size

  describe "on frozen objects" do
    before :each do
      @origin = [true, false]
      @frozen = @origin.dup.freeze
    end

    it "returns an Enumerator if no block is given" do
      @frozen.send(@method).should be_an_instance_of(Enumerator)
    end

    describe "with truthy block" do
      it "keeps elements after any exception" do
        -> { @frozen.send(@method) { true } }.should raise_error(Exception)
        @frozen.should == @origin
      end

      it "raises a FrozenError" do
        -> { @frozen.send(@method) { true } }.should raise_error(FrozenError)
      end
    end

    describe "with falsy block" do
      it "keeps elements after any exception" do
        -> { @frozen.send(@method) { false } }.should raise_error(Exception)
        @frozen.should == @origin
      end

      it "raises a FrozenError" do
        -> { @frozen.send(@method) { false } }.should raise_error(FrozenError)
      end
    end

    it "raises a FrozenError on a frozen array only during iteration if called without a block" do
      enum = @frozen.send(@method)
      -> { enum.each {} }.should raise_error(FrozenError)
    end
  end

  it "does not truncate the array is the block raises an exception" do
    a = [1, 2, 3]
    begin
      a.send(@method) { raise StandardError, 'Oops' }
    rescue
    end

    a.should == [1, 2, 3]
  end

  it "only changes elements before error is raised, keeping the element which raised an error." do
    a = [1, 2, 3, 4]
    begin
      a.send(@method) do |e|
        case e
        when 2 then false
        when 3 then raise StandardError, 'Oops'
        else true
        end
      end
    rescue StandardError
    end

    a.should == [1, 3, 4]
  end

  @value_to_return = -> (_) { true }
  it_should_behave_like :array_iterable_and_tolerating_size_increasing
end
