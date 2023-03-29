dt_load_data_file <- function(filename) {
  col_names <- c("Symbol", "Original.Receiver", "Source.Section", "CT.Address", "Builtin?", "Observed.Receiver")
  data <- fread(filename, header = FALSE, sep="\t", col.names = col_names)
  benchmark_name <- str_match(filename, "parsed\\_(.*?)\\.mylog")[2]
  data$Benchmark <- benchmark_name
  return(data)
}

dt_clean_data_file <- function(df_p, keep_blocks) {
  if (keep_blocks) {
       df_p <- data[`Builtin?` %like% "PROC|LAMBDA|block"] 
  }
  else {
       df_p <- data[!(`Builtin?` %like% "PROC|LAMBDA|block")] 
  }

  df_p <- df_p[, na.omit(.SD)][, Call.ID := 1:.N][, lapply(.SD, str_trim)]

  return (df_p)
}

dt_add_number_receivers <- function(df) { 
  data[ , 
      `:=`(Num.Receiver.Observed = n_distinct(Observed.Receiver), 
       Num.Receiver.Original = n_distinct(Original.Receiver)), 
       by=list(Source.Section, Symbol, Benchmark)]
}

dt_generate_table_one <- function(dt) {
  dt_p <- dt[ , .(Num.Calls = n_distinct(Call.ID), 
                                 Num.Call.Sites = n_distinct(Source.Section, Symbol),
                              Benchmark),
                          by = "Num.Receiver"][, dcast(.SD, 
                                                       Benchmark ~ Num.Receiver,
                                                       value.var = c("Num.Calls", "Num.Call.Sites"))]
  return(dt_p)
}

dt_analyse_splitting_transitions <- function(dt, benchmark_name) {
  split_table <- copy(data)
  split_table[ , "Num.Calls.Target" := n_distinct(Call.ID), by=list(Source.Section, Symbol, Benchmark)]
  split_table <- split_table[, .(Call.ID, Symbol, Source.Section, CT.Address, Observed.Receiver, Benchmark, Num.Calls.Target)]

  # and export it as a csv file so our java program can analyse it
  split_file <-  paste(getwd(),"/",benchmark_name,"_splitting_data.csv", sep="")
  out_split_file <- paste(getwd(),"/","out_",benchmark_name,"_splitting_data.csv", sep="")
  fwrite(split_table, split_file, row.names = FALSE)

  # call the java program to analyze the data, and then fetch back the results
  prev_wd <- getwd()
  target_dir <- file.path(getwd(), "splitting-transition","out","production","splitting-transition")
  setwd(target_dir)
  system2("java", paste(" CallSiteAnalyzer",split_file, out_split_file, sep=" "))

  setwd(prev_wd)
  row_names <- c("Source.Section", "Symbol", "Start.ID", "End.ID", "Start.State", "End.State", "Start.Cache.Size", "End.Cache.Size", "Union.Size", "Intersect.Size", "Benchmark", "Num.Calls.Target")
  transition_data <- read.csv(out_split_file, header = FALSE, sep = ",", row.names=NULL, col.names=row_names)
  return(transition_data)
}

dt_generate_table_two <- function(df, receiver_to_ditch, receveiver_to_keep, call_site_type, type_rec, keep_ct = NULL) {
  table_two <- df %>%
    select(-{{receiver_to_ditch}}, -Call.ID, -`Builtin?`) %>%
    select(Benchmark, all_of(call_site_type), {{receveiver_to_keep}}, all_of(keep_ct), {{type_rec}}) %>%
    group_by_at(c(call_site_type, "Benchmark")) %>%
    dplyr::mutate(Cache.Type = case_when({{type_rec}} == 1 ~ "MONO",
                                         {{type_rec}} > 1 & {{type_rec}} <= 8 ~ "POLY",
                                         {{type_rec}} > 8 ~ "MEGA")) %>%
    distinct() %>%
    arrange(Source.Section, Symbol, {{receveiver_to_keep}}, {{type_rec}})
  return(table_two)
}

dt_write_on_disk <- function(dt, name, path = "", bench_name = benchmark_name) {
  fwrite(dt, file.path(path,paste(bench_name,"-",name,".csv",sep="")), col.names = TRUE, row.names = FALSE, sep=" ")
}