benchs_small_names <- c("AsciidoctorConvertSmall"  = "ADConvert",
                                "AsciidoctorLoadFileSmall" = "ADLoadFile",        
                                "BlogRailsRoutesTwoRoutesTwoRequests" = "BlogRails",             
                                "ChunkyDecodePngImagePass" = "ChunkyDec",                      
                                "FannkuchRedux" = "Fannkuch",                                          
                                "ImageDemoConv" = "ImgDemoConv",
                                "ImageDemoSobel" = "ImgDemoSobel",                 
                                "LeeBench" = "Lee",
                                "PsdColorCmykToRgb" = "PsdColor",   
                                "SinatraHello" = "Sinatra")

load_all_tables <- function (folder, head = TRUE, folder_pattern="" ,patt="") { 
  result <- NULL
  
  dir <- list.dirs(folder)
  dir <- dir[ str_detect(dir, folder_pattern)]
  
  files <- sort(list.files(dir, pattern=patt, full.names = T))
  
  for (f in files) {
    options(warn=-1) # to ignore the incomplete final line warning
    data <- read.csv(f, sep=" ", strip.white = TRUE, header=head, check.names = FALSE) #check-names so no weird formatting happens
    result <- bind_rows(result, data)
  }
  result <- rename_benchs(result, benchs_small_names)
  return(result)
}

# apply_big_numbers <- function(df) {
#   df <-   df %>%
#     dplyr::mutate(across(!Benchmark, as.numeric)) %>%
#     purrr::map_df(prettyNum ,big.interval = 3,  big.mark = ",")
#   return(df)
# }

rename_benchs <- function(df, benchs) {
   df$Benchmark <- revalue(df$Benchmark, benchs)
   return (df)                              
}

apply_big_numbers <- function(df, selected_cols) {
  df <-   df %>%
    dplyr::mutate(across(selected_cols, as.numeric)) %>%
    dplyr::mutate_at(.vars = vars(selected_cols), prettyNum, big.interval = 3,  big.mark = ",")
  return(df)
}

add_percent <- function(df, list_of_col_names) {
  for (col in list_of_col_names) {
    df[[col]] <- paste(sprintf("%.1f", df[[col]]), "%", sep = "")
  }
  return (df)
}

add_percent_rounded <- function(df, list_of_col_names) {
  for (col in list_of_col_names) {
    df[[col]] <- paste(df[[col]], "%", sep = "")
  }
  return (df)
}

f0 = function(df) {
  idx = ifelse((df == 0), 0L, col(df))
  apply(idx, 1, max)
}

# From there, build Table 1
# LOC, LOC covered, Fn, Fn covered, Max receivers, number of poly calls, number of poly call-sites
sum_poly <- function(df) {
  df <- df %>% mutate(Max.Target = c(0, names(.))[f0(.) + 1])
  df$Max.Target <- sub("[^0-9]+", "", df$Max.Target)

  df$Mono.Calls <- rowSums(df[grep('^[1]_\\w+.\\w+s', names(df))])
  df$Mono.Call.Sites <- rowSums(df[grep('^[1]_\\w+.\\w+.S', names(df))])
  
  df$Poly.Calls <- rowSums(df[grep('^[2-8]_\\w+.\\w+s', names(df))])
  df$Poly.Call.Sites <- rowSums(df[grep('^[2-8]_\\w+.\\w+.S', names(df))])
  
  df$Mega.Calls <- rowSums(df[grep('^([9]|[1-9][0-9][0-9]|[1-9][0-9])_\\w+.\\w+s', names(df))])
  df$Mega.Call.Sites <- rowSums(df[grep('^([9]|[1-9][0-9][0-9]|[1-9][0-9])_\\w+.\\w+.S', names(df))])

  df$Total.Calls <- rowSums(df[grep('^([1-9]|[1-9][0-9][0-9]|[1-9][0-9])_\\w+.\\w+s', names(df))])
  df$Freq.Poly.Calls <- round(((df$Poly.Calls+df$Mega.Calls)/df$Total.Calls)*100,1)
  df$Total.Call.Sites <- rowSums(df[grep('^([1-9]|[1-9][0-9][0-9]|[1-9][0-9])_\\w+.\\w+.S', names(df))])
  df$Freq.Poly.Call.Sites <- round(((df$Poly.Call.Sites+df$Mega.Call.Sites)/df$Total.Call.Sites)*100,1)
  
  return(df %>% dplyr::mutate(across(!Benchmark, as.numeric)))
}

apply_k_columns <- function(df, col_vec) {
  for (col in col_vec) {
    df[[col]] <- round(df[[col]]/1000, 0)
  }
  return(df)
}

# Here we'll collapse together: all the PSDUtil, PSDImage, PSDCompose and ChunkyColor
combine_similar_benchmarks <- function(df, benchmarks, cluster=TRUE) {
    for (bench in benchmarks) {
      if (TRUE %in% (df$Benchmark %like% bench)) {
        table_mean <- df %>%
          filter(str_detect(Benchmark, bench)) %>%
          dplyr::mutate(across(!c(Benchmark), as.numeric)) %>%
          dplyr::summarise(across(where(is.numeric), mean)) %>%
          dplyr::mutate_if(is.numeric, round, 0) %>%
          as.data.frame()
      row.names(table_mean) <- "Mean"
      
      if (cluster) {
        cluster_aux <- df %>%
          filter(str_detect(Benchmark, bench)) %>%
          select(Cluster.Type)
        cluster_type <- unique(unlist(cluster_aux))
        table_mean$Cluster.Type <- cluster_type
      }
      
      table_mean$Benchmark <- paste(bench,"*", sep="")
      row.names(table_mean) <- NULL
      
      df <- df %>% 
        filter(!str_detect(Benchmark, bench))
        
      df <- rbind(df, table_mean)
      }
    }

  return(df %>% dplyr::arrange(Benchmark))
}


build_summary_tables <- function(df, benchmark_types, pattern="") {
  result <- df %>%
    select(Benchmark, Mono.Calls, Mono.Call.Sites, Poly.Calls, Poly.Call.Sites, Mega.Calls, Mega.Call.Sites, Max.Target) %>%
    rename_with( ~ paste0(pattern, .x), .cols = -c("Benchmark")) %>%
    merge(benchmark_types, by="Benchmark")  
}