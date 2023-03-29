build_summary_df <- function(col, benchmark_name) {
  sum_bench <- as.data.frame(t(quantile(col, probs = c(0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1)))) %>%
                  mutate("Mean" = round(mean(col),0), "Median" = round(median(col),0)) %>%
                  mutate("Min"= min(col), "Max" = max(col)) %>%
                  mutate("Benchmark" = benchmark_name)
  return(sum_bench)
}


build_distribution_plots <- function(df, metrics) {
  plot_list <- list()
  
  # collaspe together to have the different stages in one plot
  data_mod <- melt(df, id.vars='Benchmark', measure.vars=metrics)

  p <- ggplot(data_mod, aes(x=Benchmark, y=value, color=variable)) + 
      geom_boxplot() + scale_y_continuous(trans='log10') +
      ylab("Number of targets per call-site") + ggtitle("Number of different targets per call-site - distribution")    
  plot_list[[1]] <- p
  
  
  violin <-ggplot(data_mod, aes(x=Benchmark, y=value, color=variable)) +
    geom_violin() + scale_y_continuous(trans='log10') +
    ylab("Number of targets per call-site") + ggtitle("Number of different targets per call-site - distribution")
  plot_list[[2]] <- violin
  return(plot_list)
}