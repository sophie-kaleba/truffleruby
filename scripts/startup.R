
data_nos <- load_all_tables(folder_status, folder_pattern = file.path(folder_path, "NoStartup","General"), patt="one") %>% 
  replace(is.na(.), 0)
data_nos <- combine_similar_benchmarks(data_nos, c("PsdUtil","PsdCompose","PsdImage","ChunkyCanvas","ChunkyColor"))
data_nos <- join(coverage_data, data_nos) %>% select(c("Benchmark", "Times.Splitted"), everything())
names(data_nos) <- sub('(\\D+)(\\d+$)', '\\2_\\1', names(data_nos))

before_nos <- data_nos %>%
  select(1:6, contains("Before"))
names(before_nos) <- sub('(\\d+)_([a-zA-Z]+).(\\D+)_', '\\1_\\3', names(before_nos))
before_nos <- before_nos[ , gtools::mixedsort(names(before_nos))] %>% select(c("Benchmark", "Num.LOC", "LOC.Cov", "Num.Fn", "Fn.Cov", "Times.Splitted"), everything())
before_nos <- sum_poly(before_nos)

tp_nos <- data_nos %>%
  select(1:6, contains("TP"))
names(tp_nos) <- sub('(\\d+)_([a-zA-Z]+).(\\D+)_', '\\1_\\3', names(tp_nos))
tp_nos <- tp_nos[ , gtools::mixedsort(names(tp_nos))] %>% select(c("Benchmark", "Num.LOC", "LOC.Cov", "Num.Fn", "Fn.Cov", "Times.Splitted"), everything())
tp_nos <- sum_poly(tp_nos)

splitting_nos <- data_nos %>%
  select(1:6, contains("Split"))
names(splitting_nos) <- sub('(\\d+)_([a-zA-Z]+).(\\D+)_', '\\1_\\3', names(splitting_nos))
splitting_nos <- splitting_nos[ , gtools::mixedsort(names(splitting_nos))] %>% select(c("Benchmark", "Num.LOC", "LOC.Cov", "Num.Fn", "Fn.Cov", "Times.Splitted"), everything())
splitting_nos <- sum_poly(splitting_nos)

diff_tp <- (tp %>% select(-Benchmark)) - (tp_nos %>% select(-Benchmark))
diff_tp$Benchmark <- tp$Benchmark

diff_split <- (splitting %>% select(-Benchmark)) - (splitting_nos %>% select(-Benchmark))
diff_split$Benchmark <- tp$Benchmark

table_before_aux_nos <- before_nos %>%
  select(Benchmark, Mono.Calls, Mono.Call.Sites, Poly.Calls, Poly.Call.Sites, Mega.Calls, Mega.Call.Sites, Max.Target)

table_tp_aux_nos <- tp_nos %>%
  select(Benchmark, Mono.Calls, Mono.Call.Sites, Poly.Calls, Poly.Call.Sites, Mega.Calls, Mega.Call.Sites, Max.Target) %>%
  rename_with( ~ paste0("TP_", .x), .cols = -Benchmark)

table_split_aux_nos <- splitting_nos %>%
  select(Benchmark, Mono.Calls, Mono.Call.Sites, Poly.Calls, Poly.Call.Sites, Mega.Calls, Mega.Call.Sites, Max.Target) %>%
  rename_with( ~ paste0("SPLIT_", .x), .cols = -c("Benchmark")) 

table_four_max_nos <- table_before_aux_nos %>%
  merge(table_tp_aux_nos) %>%
  merge(table_split_aux_nos) %>%
  select(Benchmark, Max.Target, TP_Max.Target, SPLIT_Max.Target)

diff_max <- (table_four_max %>% select(-Benchmark)) - (table_four_max_nos %>% select(-Benchmark))
diff_max$Benchmark <- table_four_max$Benchmark

table_one_nos <- before_nos %>%
  select(Benchmark, Num.LOC, LOC.Cov, Num.Fn, Fn.Cov, Max.Target, Mono.Call.Sites, Mono.Calls, Poly.Call.Sites, Poly.Calls, Mega.Call.Sites, Mega.Calls, Total.Calls, Total.Call.Sites, Freq.Poly.Calls, Freq.Poly.Call.Sites) 
