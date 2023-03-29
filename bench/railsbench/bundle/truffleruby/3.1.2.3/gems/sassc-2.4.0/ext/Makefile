
SHELL = /bin/sh

# V=0 quiet, V=1 verbose.  other values don't work.
V = 0
V0 = $(V:0=)
Q1 = $(V:1=)
Q = $(Q1:0=@)
ECHO1 = $(V:1=@ :)
ECHO = $(ECHO1:0=@ echo)
NULLCMD = :

#### Start of system configuration section. ####

srcdir = .
topdir = /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/cext/include
hdrdir = $(topdir)
arch_hdrdir = /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/cext/include
PATH_SEPARATOR = :
VPATH = $(srcdir):$(arch_hdrdir)/ruby:$(hdrdir)/ruby:$(srcdir)/libsass/src:$(srcdir)/libsass/src/b64/:$(srcdir)/libsass/src/memory/:$(srcdir)/libsass/src/utf8/
prefix = $(DESTDIR)/home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby
exec_prefix = $(prefix)
rubylibprefix = $(libdir)/$(RUBY_BASE_NAME)
includedir = $(DESTDIR)/home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/cext
rubyarchhdrdir = $(DESTDIR)/home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/cext/include
rubyhdrdir = $(DESTDIR)/home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/cext/include
sysconfdir = $(DESTDIR)/home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/etc
bindir = $(exec_prefix)/bin
libdir = $(exec_prefix)/lib
rubylibdir = $(libdir)/mri
rubyarchdir = $(rubylibdir)
archdir = $(rubyarchdir)
sitedir = $(rubylibprefix)/site_ruby
sitehdrdir = $(rubyhdrdir)/site_ruby
sitelibdir = $(DESTDIR)./.gem.20221129-12921-yank8s
sitearchdir = $(DESTDIR)./.gem.20221129-12921-yank8s
datarootdir = $(prefix)/share
datadir = $(datarootdir)
mandir = $(datarootdir)/man
ridir = $(datarootdir)/ri
vendordir = $(rubylibprefix)/vendor_ruby


CC_WRAPPER = 
CC = /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/llvm/native/bin/graalvm-native-clang
CXX = /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/llvm/native/bin/graalvm-native-clang++
LIBRUBY = cext/libtruffleruby.so
LIBRUBY_A = 
LIBRUBYARG_SHARED = -lgraalvm-llvm
LIBRUBYARG_STATIC = 
empty =
OUTFLAG = -o $(empty)
COUTFLAG = -o $(empty)
CSRCFLAG = $(empty)

RUBY_EXTCONF_H = 
cflags   = $(optflags) $(debugflags) $(warnflags)
cxxflags = $(optflags) $(debugflags) $(warnflags)
optflags = 
debugflags = 
warnflags = 
cppflags = 
CCDLFLAGS = -fPIC
CFLAGS   = $(CCDLFLAGS) $(cflags) -DLIBSASS_VERSION='"3.6.4"' $(ARCH_FLAG)
INCFLAGS = -I. -I$(arch_hdrdir) -I$(hdrdir)/ruby/backward -I$(hdrdir) -I$(srcdir) -I$(srcdir)/libsass/include
DEFS     = -DTRUFFLERUBY_ABI_VERSION=3.1.2.3
CPPFLAGS =   $(DEFS) $(cppflags)
CXXFLAGS = $(CCDLFLAGS) $(cxxflags) -std=c++11 -DLIBSASS_VERSION='"3.6.4"' $(ARCH_FLAG)
ldflags  = 
dldflags =  -rpath /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/llvm/native/lib 
ARCH_FLAG = 
DLDFLAGS = $(ldflags) $(dldflags) $(ARCH_FLAG)
LDSHARED = $(CC) -shared
LDSHAREDXX = $(CXX) -shared
AR = /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/llvm/native/bin/ar
EXEEXT = 

RUBY_BASE_NAME = ruby
RUBY_INSTALL_NAME = truffleruby
RUBYW_INSTALL_NAME = 
RUBY_SO_NAME = ruby

arch = x86_64-linux
sitearch = $(arch)
ruby_version = 3.1.2.3
ruby = $(bindir)/truffleruby
RUBY = $(ruby)
ruby_headers = $(hdrdir)/ruby.h $(hdrdir)/ruby/backward.h $(hdrdir)/ruby/ruby.h $(hdrdir)/ruby/defines.h $(hdrdir)/ruby/missing.h $(hdrdir)/ruby/intern.h $(hdrdir)/ruby/st.h $(hdrdir)/ruby/subst.h $(arch_hdrdir)/ruby/config.h

RM = rm -f
RM_RF = rm -fr
RMDIRS = rmdir --ignore-fail-on-non-empty -p
MAKEDIRS = mkdir -p
INSTALL = /usr/bin/install -c
INSTALL_PROG = $(INSTALL) -m 0755
INSTALL_DATA = $(INSTALL) -m 0644
COPY = cp
TOUCH = exit >

#### End of system configuration section. ####

preload = 
libpath = .
LIBPATH =  -L.
DEFFILE = 

CLEANFILES = mkmf.log
DISTCLEANFILES = 
DISTCLEANDIRS = 

extout = 
extout_prefix = 
target_prefix = /sassc
LOCAL_LIBS = 
LIBS =    
ORIG_SRCS = 
SRCS = $(ORIG_SRCS) ast.cpp ast2c.cpp ast_fwd_decl.cpp ast_sel_cmp.cpp ast_sel_super.cpp ast_sel_unify.cpp ast_sel_weave.cpp ast_selectors.cpp ast_supports.cpp ast_values.cpp backtrace.cpp base64vlq.cpp bind.cpp c2ast.cpp c99func.c cencode.c check_nesting.cpp color_maps.cpp constants.cpp context.cpp cssize.cpp emitter.cpp environment.cpp error_handling.cpp eval.cpp eval_selectors.cpp expand.cpp extender.cpp extension.cpp file.cpp fn_colors.cpp fn_lists.cpp fn_maps.cpp fn_miscs.cpp fn_numbers.cpp fn_selectors.cpp fn_strings.cpp fn_utils.cpp inspect.cpp json.cpp lexer.cpp listize.cpp allocator.cpp shared_ptr.cpp operators.cpp output.cpp parser.cpp parser_selectors.cpp plugins.cpp position.cpp prelexer.cpp remove_placeholders.cpp sass.cpp sass2scss.cpp sass_context.cpp sass_functions.cpp sass_values.cpp source.cpp source_map.cpp stylesheet.cpp to_value.cpp units.cpp utf8_string.cpp util.cpp util_string.cpp values.cpp
OBJS = ast.o ast2c.o ast_fwd_decl.o ast_sel_cmp.o ast_sel_super.o ast_sel_unify.o ast_sel_weave.o ast_selectors.o ast_supports.o ast_values.o backtrace.o base64vlq.o bind.o c2ast.o c99func.o cencode.o check_nesting.o color_maps.o constants.o context.o cssize.o emitter.o environment.o error_handling.o eval.o eval_selectors.o expand.o extender.o extension.o file.o fn_colors.o fn_lists.o fn_maps.o fn_miscs.o fn_numbers.o fn_selectors.o fn_strings.o fn_utils.o inspect.o json.o lexer.o listize.o allocator.o shared_ptr.o operators.o output.o parser.o parser_selectors.o plugins.o position.o prelexer.o remove_placeholders.o sass.o sass2scss.o sass_context.o sass_functions.o sass_values.o source.o source_map.o stylesheet.o to_value.o units.o utf8_string.o util.o util_string.o values.o
HDRS = 
LOCAL_HDRS = 
TARGET = libsass
TARGET_NAME = libsass
TARGET_ENTRY = Init_$(TARGET_NAME)
DLLIB = $(TARGET).so
EXTSTATIC = 
STATIC_LIB = 

TIMESTAMP_DIR = .
BINDIR        = $(bindir)
RUBYCOMMONDIR = $(sitedir)$(target_prefix)
RUBYLIBDIR    = $(sitelibdir)$(target_prefix)
RUBYARCHDIR   = $(sitearchdir)$(target_prefix)
HDRDIR        = $(sitehdrdir)$(target_prefix)
ARCHHDRDIR    = $(sitearchhdrdir)$(target_prefix)
TARGET_SO_DIR =
TARGET_SO     = $(TARGET_SO_DIR)$(DLLIB)
CLEANLIBS     = $(TARGET_SO) 
CLEANOBJS     = *.o  *.bak

all:    $(DLLIB)
static: $(STATIC_LIB)
.PHONY: all install static install-so install-rb
.PHONY: clean clean-so clean-static clean-rb

clean-static::
clean-rb-default::
clean-rb::
clean-so::
clean: clean-so clean-static clean-rb-default clean-rb
		-$(Q)$(RM_RF) $(CLEANLIBS) $(CLEANOBJS) $(CLEANFILES) .*.time

distclean-rb-default::
distclean-rb::
distclean-so::
distclean-static::
distclean: clean distclean-so distclean-static distclean-rb-default distclean-rb
		-$(Q)$(RM) Makefile $(RUBY_EXTCONF_H) conftest.* mkmf.log
		-$(Q)$(RM) core ruby$(EXEEXT) *~ $(DISTCLEANFILES)
		-$(Q)$(RMDIRS) $(DISTCLEANDIRS) 2> /dev/null || true

realclean: distclean
install: install-so install-rb

install-so: $(DLLIB) $(TIMESTAMP_DIR)/.sitearchdir.-.sassc.time
	$(INSTALL_PROG) $(DLLIB) $(RUBYARCHDIR)
clean-static::
	-$(Q)$(RM) $(STATIC_LIB)
install-rb: pre-install-rb do-install-rb install-rb-default
install-rb-default: pre-install-rb-default do-install-rb-default
pre-install-rb: Makefile
pre-install-rb-default: Makefile
do-install-rb:
do-install-rb-default:
pre-install-rb-default:
	@$(NULLCMD)
$(TIMESTAMP_DIR)/.sitearchdir.-.sassc.time:
	$(Q) $(MAKEDIRS) $(@D) $(RUBYARCHDIR)
	$(Q) $(TOUCH) $@

site-install: site-install-so site-install-rb
site-install-so: install-so
site-install-rb: install-rb

.SUFFIXES: .c .m .cc .mm .cxx .cpp .o .S

.cc.o:
	$(ECHO) compiling $(<)
	$(Q) $(if\
  $(or\
    $(and\
      $(findstring json/ext/parser, $(realpath $(<))),\
      $(or\
        $(findstring parser.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /nokogiri, $(realpath $(<))),\
      $(or\
        $(findstring xml_node_set.c, $(<)),\
        $(findstring xslt_stylesheet.c, $(<)),\
        $(findstring xml_document.c, $(<)),\
        $(findstring xml_sax_parser.c, $(<)),\
        $(findstring xml_xpath_context.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ruby-oci8/ext/oci8, $(realpath $(<))),\
      $(or\
        $(findstring ocinumber.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /pg, $(realpath $(<))),\
      $(or\
        $(findstring pg_result.c, $(<)),\
        $(findstring pg_tuple.c, $(<)),\
        $(findstring pg_text_decoder.c, $(<)),\
        $(findstring pg_text_encoder.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ext/tk/tkutil, $(realpath $(<))),\
      $(or\
        $(findstring tkutil.c, $(<)),\
      )\
    ),\
  ),\
  /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/bin/truffleruby /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/cext/preprocess.rb $< $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c | $(CXX) -I$(<D) $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c -xc++ -,\
  $(CXX) $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c $(CSRCFLAG)$<\
)


.cc.S:
	$(ECHO) translating $(<)
	$(Q) $(if\
  $(or\
    $(and\
      $(findstring json/ext/parser, $(realpath $(<))),\
      $(or\
        $(findstring parser.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /nokogiri, $(realpath $(<))),\
      $(or\
        $(findstring xml_node_set.c, $(<)),\
        $(findstring xslt_stylesheet.c, $(<)),\
        $(findstring xml_document.c, $(<)),\
        $(findstring xml_sax_parser.c, $(<)),\
        $(findstring xml_xpath_context.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ruby-oci8/ext/oci8, $(realpath $(<))),\
      $(or\
        $(findstring ocinumber.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /pg, $(realpath $(<))),\
      $(or\
        $(findstring pg_result.c, $(<)),\
        $(findstring pg_tuple.c, $(<)),\
        $(findstring pg_text_decoder.c, $(<)),\
        $(findstring pg_text_encoder.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ext/tk/tkutil, $(realpath $(<))),\
      $(or\
        $(findstring tkutil.c, $(<)),\
      )\
    ),\
  ),\
  /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/bin/truffleruby /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/cext/preprocess.rb $< $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -S | $(CXX) -I$(<D) $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c -xc++ -,\
  $(CXX) $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c $(CSRCFLAG)$<\
)


.mm.o:
	$(ECHO) compiling $(<)
	$(Q) $(if\
  $(or\
    $(and\
      $(findstring json/ext/parser, $(realpath $(<))),\
      $(or\
        $(findstring parser.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /nokogiri, $(realpath $(<))),\
      $(or\
        $(findstring xml_node_set.c, $(<)),\
        $(findstring xslt_stylesheet.c, $(<)),\
        $(findstring xml_document.c, $(<)),\
        $(findstring xml_sax_parser.c, $(<)),\
        $(findstring xml_xpath_context.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ruby-oci8/ext/oci8, $(realpath $(<))),\
      $(or\
        $(findstring ocinumber.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /pg, $(realpath $(<))),\
      $(or\
        $(findstring pg_result.c, $(<)),\
        $(findstring pg_tuple.c, $(<)),\
        $(findstring pg_text_decoder.c, $(<)),\
        $(findstring pg_text_encoder.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ext/tk/tkutil, $(realpath $(<))),\
      $(or\
        $(findstring tkutil.c, $(<)),\
      )\
    ),\
  ),\
  /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/bin/truffleruby /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/cext/preprocess.rb $< $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c | $(CXX) -I$(<D) $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c -xc++ -,\
  $(CXX) $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c $(CSRCFLAG)$<\
)


.mm.S:
	$(ECHO) translating $(<)
	$(Q) $(if\
  $(or\
    $(and\
      $(findstring json/ext/parser, $(realpath $(<))),\
      $(or\
        $(findstring parser.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /nokogiri, $(realpath $(<))),\
      $(or\
        $(findstring xml_node_set.c, $(<)),\
        $(findstring xslt_stylesheet.c, $(<)),\
        $(findstring xml_document.c, $(<)),\
        $(findstring xml_sax_parser.c, $(<)),\
        $(findstring xml_xpath_context.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ruby-oci8/ext/oci8, $(realpath $(<))),\
      $(or\
        $(findstring ocinumber.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /pg, $(realpath $(<))),\
      $(or\
        $(findstring pg_result.c, $(<)),\
        $(findstring pg_tuple.c, $(<)),\
        $(findstring pg_text_decoder.c, $(<)),\
        $(findstring pg_text_encoder.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ext/tk/tkutil, $(realpath $(<))),\
      $(or\
        $(findstring tkutil.c, $(<)),\
      )\
    ),\
  ),\
  /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/bin/truffleruby /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/cext/preprocess.rb $< $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -S | $(CXX) -I$(<D) $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c -xc++ -,\
  $(CXX) $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c $(CSRCFLAG)$<\
)


.cxx.o:
	$(ECHO) compiling $(<)
	$(Q) $(if\
  $(or\
    $(and\
      $(findstring json/ext/parser, $(realpath $(<))),\
      $(or\
        $(findstring parser.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /nokogiri, $(realpath $(<))),\
      $(or\
        $(findstring xml_node_set.c, $(<)),\
        $(findstring xslt_stylesheet.c, $(<)),\
        $(findstring xml_document.c, $(<)),\
        $(findstring xml_sax_parser.c, $(<)),\
        $(findstring xml_xpath_context.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ruby-oci8/ext/oci8, $(realpath $(<))),\
      $(or\
        $(findstring ocinumber.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /pg, $(realpath $(<))),\
      $(or\
        $(findstring pg_result.c, $(<)),\
        $(findstring pg_tuple.c, $(<)),\
        $(findstring pg_text_decoder.c, $(<)),\
        $(findstring pg_text_encoder.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ext/tk/tkutil, $(realpath $(<))),\
      $(or\
        $(findstring tkutil.c, $(<)),\
      )\
    ),\
  ),\
  /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/bin/truffleruby /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/cext/preprocess.rb $< $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c | $(CXX) -I$(<D) $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c -xc++ -,\
  $(CXX) $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c $(CSRCFLAG)$<\
)


.cxx.S:
	$(ECHO) translating $(<)
	$(Q) $(if\
  $(or\
    $(and\
      $(findstring json/ext/parser, $(realpath $(<))),\
      $(or\
        $(findstring parser.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /nokogiri, $(realpath $(<))),\
      $(or\
        $(findstring xml_node_set.c, $(<)),\
        $(findstring xslt_stylesheet.c, $(<)),\
        $(findstring xml_document.c, $(<)),\
        $(findstring xml_sax_parser.c, $(<)),\
        $(findstring xml_xpath_context.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ruby-oci8/ext/oci8, $(realpath $(<))),\
      $(or\
        $(findstring ocinumber.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /pg, $(realpath $(<))),\
      $(or\
        $(findstring pg_result.c, $(<)),\
        $(findstring pg_tuple.c, $(<)),\
        $(findstring pg_text_decoder.c, $(<)),\
        $(findstring pg_text_encoder.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ext/tk/tkutil, $(realpath $(<))),\
      $(or\
        $(findstring tkutil.c, $(<)),\
      )\
    ),\
  ),\
  /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/bin/truffleruby /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/cext/preprocess.rb $< $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -S | $(CXX) -I$(<D) $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c -xc++ -,\
  $(CXX) $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c $(CSRCFLAG)$<\
)


.cpp.o:
	$(ECHO) compiling $(<)
	$(Q) $(if\
  $(or\
    $(and\
      $(findstring json/ext/parser, $(realpath $(<))),\
      $(or\
        $(findstring parser.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /nokogiri, $(realpath $(<))),\
      $(or\
        $(findstring xml_node_set.c, $(<)),\
        $(findstring xslt_stylesheet.c, $(<)),\
        $(findstring xml_document.c, $(<)),\
        $(findstring xml_sax_parser.c, $(<)),\
        $(findstring xml_xpath_context.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ruby-oci8/ext/oci8, $(realpath $(<))),\
      $(or\
        $(findstring ocinumber.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /pg, $(realpath $(<))),\
      $(or\
        $(findstring pg_result.c, $(<)),\
        $(findstring pg_tuple.c, $(<)),\
        $(findstring pg_text_decoder.c, $(<)),\
        $(findstring pg_text_encoder.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ext/tk/tkutil, $(realpath $(<))),\
      $(or\
        $(findstring tkutil.c, $(<)),\
      )\
    ),\
  ),\
  /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/bin/truffleruby /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/cext/preprocess.rb $< $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c | $(CXX) -I$(<D) $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c -xc++ -,\
  $(CXX) $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c $(CSRCFLAG)$<\
)


.cpp.S:
	$(ECHO) translating $(<)
	$(Q) $(if\
  $(or\
    $(and\
      $(findstring json/ext/parser, $(realpath $(<))),\
      $(or\
        $(findstring parser.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /nokogiri, $(realpath $(<))),\
      $(or\
        $(findstring xml_node_set.c, $(<)),\
        $(findstring xslt_stylesheet.c, $(<)),\
        $(findstring xml_document.c, $(<)),\
        $(findstring xml_sax_parser.c, $(<)),\
        $(findstring xml_xpath_context.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ruby-oci8/ext/oci8, $(realpath $(<))),\
      $(or\
        $(findstring ocinumber.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /pg, $(realpath $(<))),\
      $(or\
        $(findstring pg_result.c, $(<)),\
        $(findstring pg_tuple.c, $(<)),\
        $(findstring pg_text_decoder.c, $(<)),\
        $(findstring pg_text_encoder.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ext/tk/tkutil, $(realpath $(<))),\
      $(or\
        $(findstring tkutil.c, $(<)),\
      )\
    ),\
  ),\
  /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/bin/truffleruby /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/cext/preprocess.rb $< $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -S | $(CXX) -I$(<D) $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c -xc++ -,\
  $(CXX) $(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c $(CSRCFLAG)$<\
)


.c.o:
	$(ECHO) compiling $(<)
	$(Q) $(if\
  $(or\
    $(and\
      $(findstring json/ext/parser, $(realpath $(<))),\
      $(or\
        $(findstring parser.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /nokogiri, $(realpath $(<))),\
      $(or\
        $(findstring xml_node_set.c, $(<)),\
        $(findstring xslt_stylesheet.c, $(<)),\
        $(findstring xml_document.c, $(<)),\
        $(findstring xml_sax_parser.c, $(<)),\
        $(findstring xml_xpath_context.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ruby-oci8/ext/oci8, $(realpath $(<))),\
      $(or\
        $(findstring ocinumber.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /pg, $(realpath $(<))),\
      $(or\
        $(findstring pg_result.c, $(<)),\
        $(findstring pg_tuple.c, $(<)),\
        $(findstring pg_text_decoder.c, $(<)),\
        $(findstring pg_text_encoder.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ext/tk/tkutil, $(realpath $(<))),\
      $(or\
        $(findstring tkutil.c, $(<)),\
      )\
    ),\
  ),\
  /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/bin/truffleruby /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/cext/preprocess.rb $< $(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG)$@ -c | $(CC) -I$(<D) $(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG)$@ -c -xc -,\
  $(CC) $(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG)$@ -c $(CSRCFLAG)$<\
)


.c.S:
	$(ECHO) translating $(<)
	$(Q) $(if\
  $(or\
    $(and\
      $(findstring json/ext/parser, $(realpath $(<))),\
      $(or\
        $(findstring parser.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /nokogiri, $(realpath $(<))),\
      $(or\
        $(findstring xml_node_set.c, $(<)),\
        $(findstring xslt_stylesheet.c, $(<)),\
        $(findstring xml_document.c, $(<)),\
        $(findstring xml_sax_parser.c, $(<)),\
        $(findstring xml_xpath_context.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ruby-oci8/ext/oci8, $(realpath $(<))),\
      $(or\
        $(findstring ocinumber.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /pg, $(realpath $(<))),\
      $(or\
        $(findstring pg_result.c, $(<)),\
        $(findstring pg_tuple.c, $(<)),\
        $(findstring pg_text_decoder.c, $(<)),\
        $(findstring pg_text_encoder.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ext/tk/tkutil, $(realpath $(<))),\
      $(or\
        $(findstring tkutil.c, $(<)),\
      )\
    ),\
  ),\
  /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/bin/truffleruby /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/cext/preprocess.rb $< $(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG)$@ -S | $(CC) -I$(<D) $(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG)$@ -c -xc -,\
  $(CC) $(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG)$@ -c $(CSRCFLAG)$<\
)


.m.o:
	$(ECHO) compiling $(<)
	$(Q) $(if\
  $(or\
    $(and\
      $(findstring json/ext/parser, $(realpath $(<))),\
      $(or\
        $(findstring parser.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /nokogiri, $(realpath $(<))),\
      $(or\
        $(findstring xml_node_set.c, $(<)),\
        $(findstring xslt_stylesheet.c, $(<)),\
        $(findstring xml_document.c, $(<)),\
        $(findstring xml_sax_parser.c, $(<)),\
        $(findstring xml_xpath_context.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ruby-oci8/ext/oci8, $(realpath $(<))),\
      $(or\
        $(findstring ocinumber.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /pg, $(realpath $(<))),\
      $(or\
        $(findstring pg_result.c, $(<)),\
        $(findstring pg_tuple.c, $(<)),\
        $(findstring pg_text_decoder.c, $(<)),\
        $(findstring pg_text_encoder.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ext/tk/tkutil, $(realpath $(<))),\
      $(or\
        $(findstring tkutil.c, $(<)),\
      )\
    ),\
  ),\
  /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/bin/truffleruby /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/cext/preprocess.rb $< $(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG)$@ -c | $(CC) -I$(<D) $(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG)$@ -c -xc -,\
  $(CC) $(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG)$@ -c $(CSRCFLAG)$<\
)


.m.S:
	$(ECHO) translating $(<)
	$(Q) $(if\
  $(or\
    $(and\
      $(findstring json/ext/parser, $(realpath $(<))),\
      $(or\
        $(findstring parser.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /nokogiri, $(realpath $(<))),\
      $(or\
        $(findstring xml_node_set.c, $(<)),\
        $(findstring xslt_stylesheet.c, $(<)),\
        $(findstring xml_document.c, $(<)),\
        $(findstring xml_sax_parser.c, $(<)),\
        $(findstring xml_xpath_context.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ruby-oci8/ext/oci8, $(realpath $(<))),\
      $(or\
        $(findstring ocinumber.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring /pg, $(realpath $(<))),\
      $(or\
        $(findstring pg_result.c, $(<)),\
        $(findstring pg_tuple.c, $(<)),\
        $(findstring pg_text_decoder.c, $(<)),\
        $(findstring pg_text_encoder.c, $(<)),\
      )\
    ),\
    $(and\
      $(findstring ext/tk/tkutil, $(realpath $(<))),\
      $(or\
        $(findstring tkutil.c, $(<)),\
      )\
    ),\
  ),\
  /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/bin/truffleruby /home/sopi/Documents/Side_projects/graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/cext/preprocess.rb $< $(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG)$@ -S | $(CC) -I$(<D) $(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG)$@ -c -xc -,\
  $(CC) $(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG)$@ -c $(CSRCFLAG)$<\
)


$(TARGET_SO): $(OBJS) Makefile
	$(ECHO) linking shared-object sassc/$(DLLIB)
	-$(Q)$(RM) $(@)
	$(Q) $(LDSHAREDXX) -o $@ $(OBJS) $(LIBPATH) $(DLDFLAGS) $(LOCAL_LIBS) $(LIBS)
	$(Q) strip -x $@



###
$(OBJS): Makefile

# Replaces default mkmf dependencies. Default mkmf dependencies include all libruby headers.
# We don't need libruby and some of these headers are missing on JRuby (breaking compilation there).
$(OBJS): $(HDRS)

