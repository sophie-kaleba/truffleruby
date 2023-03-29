writeLines("[INFO] Loading Libraries")

load_and_install_if_necessary <- function(package_name) {
  if (!suppressPackageStartupMessages(library(package_name, character.only=TRUE, logical.return=TRUE))) {
    cat(paste0("Package ", package_name, " not found. Will install it."))
    install.packages(package_name)
    library(package_name, character.only=TRUE)
  }
}

load_and_install_if_necessary("plyr")
load_and_install_if_necessary("dplyr")
load_and_install_if_necessary("ggplot2")
load_and_install_if_necessary("psych")   # uses only geometric.mean
load_and_install_if_necessary("tables")
load_and_install_if_necessary("reshape2")
load_and_install_if_necessary("gtools")
#load_and_install_if_necessary("assertthat")
load_and_install_if_necessary("tikzDevice")
load_and_install_if_necessary("scales")
load_and_install_if_necessary("memoise")
load_and_install_if_necessary("RColorBrewer")
load_and_install_if_necessary("ggrepel")   # make sure labels don't overlap
load_and_install_if_necessary("forcats")
load_and_install_if_necessary("stringr")
load_and_install_if_necessary("xtable")
load_and_install_if_necessary("htmlTable")
load_and_install_if_necessary("testit")
load_and_install_if_necessary("assertr")
load_and_install_if_necessary("R.utils")
load_and_install_if_necessary("networkD3")
load_and_install_if_necessary("janitor")
load_and_install_if_necessary('beanplot')

if (!suppressPackageStartupMessages(library("data.table", character.only=TRUE, logical.return=TRUE))) {
  cat(paste0("Package ", "data.table", " not found. Will install it."))
  install.packages("data.table", repos="http://R-Forge.R-project.org")
  library("data.table", character.only=TRUE)
}


source("data-processing.R")
source("plots.R")
source("colors.R")
source("paper.R")
source("phase-data-processing.R")
source("machine_specs.R")
source("behaviour-processing.R")
source("target-polymorphism.R")
source("splitting.R")
source("performance-methods.R")
source("distribution.R")

is.empty <- function(x, mode=NULL,...){
  
  if(is.null(x)) {
    warning("x is NULL")
    return(FALSE)
  }
  
  if(is.null(mode)) mode <- class(x)
  identical(vector(mode,1),c(x,vector(class(x),1)))
}

# avoid scientific notation for numbers, it's more readable to me
options(scipen=999)

# prints stack trace on error, from: http://stackoverflow.com/a/2000757/916546
options(warn = 2, keep.source = TRUE, error = 
          quote({ 
            cat("Environment:\n", file=stderr()); 
            
            # TODO: setup option for dumping to a file (?)
            # Set `to.file` argument to write this to a file for post-mortem debugging    
            dump.frames();  # writes to last.dump
            
            #
            # Debugging in R
            #   http://www.stats.uwo.ca/faculty/murdoch/software/debuggingR/index.shtml
            #
            # Post-mortem debugging
            #   http://www.stats.uwo.ca/faculty/murdoch/software/debuggingR/pmd.shtml
            #
            # Relation functions:
            #   dump.frames
            #   recover
            # >>limitedLabels  (formatting of the dump with source/line numbers)
            #   sys.frame (and associated)
            #   traceback
            #   geterrmessage
            #
            # Output based on the debugger function definition.
            
            n <- length(last.dump)
            calls <- names(last.dump)
            cat(paste("  ", 1L:n, ": ", calls, sep = ""), sep = "\n", file=stderr())
            cat("\n", file=stderr())
            
            if (!interactive()) {
              q(status=1) # indicate error
            }
          }))

