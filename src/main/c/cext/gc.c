/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
#include <truffleruby-impl.h>
#include <internal/gc.h>

// GC, rb_gc_*

void rb_global_variable(VALUE *address) {
  rb_gc_register_address(address);
}

void rb_gc_register_address(VALUE *address) {
  /* NOTE: this captures the value after the Init_ function returns and assumes the value does not change after that. */
  polyglot_invoke(RUBY_CEXT, "rb_gc_register_address", address);
}

void rb_gc_unregister_address(VALUE *address) {
  polyglot_invoke(RUBY_CEXT, "rb_gc_unregister_address", address);
}

void rb_gc_mark(VALUE ptr) {
  polyglot_invoke(RUBY_CEXT, "rb_gc_mark", ptr);
}

void rb_gc_mark_maybe(VALUE ptr) {
  if (!RB_TYPE_P(ptr, T_NONE)) {
    polyglot_invoke(RUBY_CEXT, "rb_gc_mark", ptr);
  }
}

VALUE rb_gc_enable() {
  return RUBY_CEXT_INVOKE("rb_gc_enable");
}

VALUE rb_gc_disable() {
  return RUBY_CEXT_INVOKE("rb_gc_disable");
}

void rb_gc(void) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_gc");
}

void rb_gc_force_recycle(VALUE obj) {
  // Comments in MRI imply rb_gc_force_recycle functions as a GC guard
  RB_GC_GUARD(obj);
}

VALUE rb_gc_latest_gc_info(VALUE key) {
  return RUBY_CEXT_INVOKE("rb_gc_latest_gc_info", key);
}

void rb_gc_register_mark_object(VALUE obj) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_gc_register_mark_object", obj);
}

void* rb_tr_read_VALUE_pointer(VALUE *pointer) {
  VALUE value = *pointer;
  return rb_tr_unwrap(value);
}
