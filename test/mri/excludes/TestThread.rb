exclude :test_no_valid_cfp, "needs investigation"
exclude :test_thread_join_current, "test hangs"
exclude :test_thread_join_main_thread, "test hangs"
exclude :test_abort_on_exception, "needs investigation"
exclude :test_handle_interrupt_and_io, "needs investigation"
exclude :test_handle_interrupt_blocking, "needs investigation"
exclude :test_handle_interrupt_invalid_argument, "needs investigation"
exclude :test_handle_interrupted?, "needs investigation"
exclude :test_kill_wrong_argument, "needs investigation"
exclude :test_machine_stack_size, "needs investigation"
exclude :test_main_thread_status_at_exit, "needs investigation"
exclude :test_priority, "needs investigation"
exclude :test_thread_invalid_name, "needs investigation"
exclude :test_thread_timer_and_interrupt, "needs investigation"
exclude :test_vm_machine_stack_size, "needs investigation"
exclude :test_stack_size, "needs investigation"
exclude :test_stop, "needs investigation"
exclude :test_thread_interrupt_for_killed_thread, "needs investigation"
exclude :test_uninitialized, "needs investigation"
exclude :test_wakeup, "needs investigation"
exclude :test_handle_interrupt, "needs investigation"
exclude :test_ignore_deadlock, "needs investigation"
exclude :test_switch_while_busy_loop, "transient"
exclude :test_handle_interrupt_and_p, "transient"
exclude :test_thread_value_in_trap, "transient"
exclude :test_join_argument_conversion, "TypeError: TruffleRuby doesn't have a case for the org.truffleruby.core.thread.ThreadNodesFactory$JoinNodeFactory$JoinNodeGen node with values of type TestThread::Thread(org.truffleruby.core.thread.RubyThread) #<Class:0xaf5f38>(org.truffleruby.core.basicobject.RubyBasicObject)"
exclude :test_inspect, "<\"#<#<Module:0x34d1f8>::Cスレッド:0x34d208 /Users/graal/slave/e/main/test/mri/tests/ruby/test_thread.rb:33 run>\"> expected but was"
