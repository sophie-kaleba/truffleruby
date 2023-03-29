## This file defines common functions used for data processing.

#shared functions
add_call_id <- function(df) {
  df <- df %>% 
    mutate(Number = rownames(df))
  return (df)
}

plot_and_save <- function(df, dimension, file_name, dirname, plot_fun) {
for (var in unique(df$dimension)) {
    p_ <- plot_fun(var)
    ggsave(p_, path=dirname, filename=file_name, width = 250, units="mm")
}
}

simple_morse_plot <- function(df, x_data, y_data, z_data, xlab_text, ylab_text, text_size, title) {
  p <- ggplot(data = df, aes(x=as.numeric(as.character(x_data)), y=y_data, color=z_data)) + geom_point(size = 0.5)
  p <- p  + xlab(xlab_text) + ylab(ylab_text) + theme(legend.position = "none") + ggtitle(title)
  p <- p + theme(text = element_text(size=text_size)) 
  return (p)
}

simple_facet_morse_plot <- function(datafile, x_data, y_data, z_data, facet_data, xlab_text, ylab_text, text_size, title) {
  p <- simple_morse_plot(datafile, x_data, y_data, z_data, xlab_text, ylab_text, text_size, title)
  p <- p + facet_wrap( ~(facet_data), ncol = 3) 
  return(p)
}


simple_facet_plot <- function(datafile, x_data, y_data, z_data, facet_data, xlab_text, ylab_text, text_size) {
  p <- ggplot(data = datafile, aes(x=as.numeric(as.character(x_data)), y=y_data, color=z_data)) + geom_point(size = 0.5)
  p <- p + facet_wrap( ~(facet_data), ncol = 3) + xlab(xlab_text) + ylab(ylab_text) + theme(legend.position = "none") 
  p <- p + theme(text = element_text(size=text_size))
  return(p)
}

read_simple_profiling_file <- function(filename) {
  row_names <- c("Symbol", "Receiver", "Source.Section")

  data <- read.csv(filename, header = FALSE, sep = "\t", row.names=NULL, col.names=row_names)
  return(data)
}

read_splitting_profiling_file <- function(filename) {
  row_names <- c("Symbol", "Original.Receiver", "Source.Section", "CT.Address", "Builtin?", "Observed.Receiver")

  data <- read.csv(filename, header = FALSE, sep = "\t", row.names=NULL, col.names=row_names)
  
  benchmark_name <- str_match(filename, "parsed\\_(.*?)\\.mylog")[2]
  data$Benchmark <- benchmark_name
  return(data)
}

read_test_data <- function(filename) {
  data <- readr::read.csv(filename, header = TRUE, sep = ",")
  return(data)
}

#Closures
read_closure_file <- function(filename) {
  row_names <- c("Source.Section", "Call.Target", "Call.Target.Short")
  
  data_closure <- read.csv(filename, header = FALSE, sep = "\t", row.names=NULL, col.names=row_names)

  # add an id to each call
  data_closure <- add_call_id(data_closure)
  return(data_closure)
}




