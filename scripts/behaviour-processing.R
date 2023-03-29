Call.Site.Target <- c("Source.Section", "Symbol", "Call.Site.ID", "CT.Address")
Call.Site <- c("Source.Section", "Symbol", "Call.Site.ID")

Block.Call.Site.Target <- c("Observed.Receiver", "CT.Address")
Block.Call.Site <- c("Observed.Receiver")

load_all_data <- function (folder, keep_blocks) { 
  result <- NULL
  files <- sort(list.files(folder))
  
  for (f in files) {
    data <- load_data_file(paste(folder, f, sep = "/"))
    data <- clean_data_file(data, keep_blocks)
    result <- rbind(result, data)
  }
  return(result)
}


load_data_file <- function(filename) {
  read_splitting_profiling_file(filename)  
}

count_things <- function(df, grouped) {
    n_distinct(df %>% dplyr::select(all_of(grouped))) 
}

compute_weight <- function(subset, total, digits=0) {
  round((subset / total) * 100, digits)
}

clean_data_file <- function(df_p, keep_blocks) {
  if (keep_blocks) {
    df <- df_p %>%
      dplyr::filter(Builtin. =="PROC" | Builtin. =="LAMBDA" | Builtin. =="block" ) 
  }
  else {
    df <- df_p %>%
       dplyr::filter(!(Builtin. =="PROC" | Builtin. =="LAMBDA" | Builtin. =="block")) 
  }
  
  gc()

  df <- df %>% 
    dplyr::filter(!(Source.Section=="")) %>% 
    tibble::rowid_to_column(var="Call.ID") %>%
    mutate(across(where(is.character), str_trim)) #trim all columns
  df <- as_tibble(df)
  return (df)
}


add_number_receivers <- function(df, call_site) {
    df %>%
    group_by_at(call_site) %>%
    dplyr::mutate(Num.Receiver.Observed = n_distinct(Observed.Receiver)) %>%
    dplyr::mutate(Num.Receiver.Original = n_distinct(Original.Receiver)) %>%
    ungroup() 
}

#' Return a data frame that summarises polymoprhic behaviour in this run, takes splitting into account
# Add: Num.Call.Sites (the number of call-sites associated with a given number of receivers), Cumulative
#' @param num_receiver_column, whether we consider the observed or the original number of receivers  
#' @param ct_address, whether we consider splitting 
compute_num_target_details <- function(df_p, call_site_type, receiver_type) {
  df <- df_p %>%
    select(c(all_of(call_site_type), !! sym(receiver_type), Call.ID), "Benchmark") %>% 
    dplyr::group_by_at(c(all_of(call_site_type), "Benchmark")) %>%
    dplyr::summarise(Num.Receiver = dplyr::n_distinct(!! sym(receiver_type)), Num.Calls = n_distinct(Call.ID)) %>%
    group_by(Num.Receiver, Benchmark)  %>%
    dplyr::summarise(Num.Call.Sites = n(), Num.Calls=sum(Num.Calls)) %>%
    group_by(Benchmark) %>%
    dplyr::mutate(Cumulative.Call.Sites = rev(cumsum(rev(Num.Call.Sites)))) %>%
    dplyr::mutate(Cumulative.Calls = rev(cumsum(rev(Num.Calls)))) %>%
    dplyr::mutate(Frequency = round(Num.Calls/sum(Num.Calls),7)*100)  %>%
    dplyr::mutate(Cumulative.Freq = rev(cumsum(rev(Frequency))))
  return(df)
}

#' Return a data frame that summarises polymoprhic behaviour in this run, takes splitting into account
# Add: Num.Call.Sites (the number of call-sites associated with a given number of receivers), Cumulative
#' @param num_receiver_column, whether we consider the observed or the original number of receivers  
#' @param ct_address, whether we consider splitting 
compute_num_target_global <- function(df_p, call_site_type, receiver_type) {
  df <- df_p %>%
    select(c(all_of(call_site_type), !! sym(receiver_type), Call.ID), "Benchmark") %>% 
    dplyr::group_by_at(c(all_of(call_site_type), "Benchmark")) %>%
    dplyr::summarise(Num.Receiver = dplyr::n_distinct(!! sym(receiver_type)), Num.Calls = n_distinct(Call.ID)) %>%
    group_by(Num.Receiver, Benchmark)  %>%
    dplyr::summarise(Num.Call.Sites = n(), Num.Calls=sum(Num.Calls)) %>%
    group_by(Benchmark) %>%
    dplyr::mutate(Cumulative.Call.Sites = rev(cumsum(rev(Num.Call.Sites)))) %>%
    dplyr::mutate(Cumulative.Calls = rev(cumsum(rev(Num.Calls)))) %>%
    dplyr::mutate(Frequency = round(Num.Calls/sum(Num.Calls),7)*100)  %>%
    dplyr::mutate(Cumulative.Freq = rev(cumsum(rev(Frequency))))
  return(df)
}

add_lookup_status_per_call <- function(df_p) {
  df <- df_p %>%
    ungroup() %>%
    group_by_at(c(Call.Site.Target, "Benchmark")) %>%
    dplyr::mutate(Cache.Type.Original = case_when(Num.Receiver.Original == 1 ~ "MONO",
                                        Num.Receiver.Original > 1 & Num.Receiver.Original <= 8 ~ "POLY",
                                        Num.Receiver.Original > 8 ~ "MEGA"))%>%
    dplyr::mutate(Cache.Type.Observed = case_when(Num.Receiver.Observed == 1 ~ "MONO",
                                        Num.Receiver.Observed > 1 & Num.Receiver.Observed <= 8 ~ "POLY",
                                        Num.Receiver.Observed > 8 ~ "MEGA")) 
  return(df)
}

# cluster_benchmarks <- function(df) {
#   df <- df %>%
#     dplyr::mutate(Cluster.Type = case_when(Freq.Poly.Calls <= 0.5 ~ "Monomorphic",
#                                           Freq.Poly.Calls > 0.5 & Freq.Poly.Calls <= 5 ~ "Polymorphic.Moderate",
#                                           Freq.Poly.Calls > 5 ~ "Polymorphic.Significant"))
#   return(df)
# }

cluster_benchmarks <- function(df) {
  df <- df %>%
    dplyr::mutate(Cluster.Type = case_when(Mega.Call.Sites > 0 ~ "Megamorphic",
                                          Poly.Call.Sites > 0 & Freq.Poly.Call.Sites >= 1.5  ~ "Polymorphic.Medium",
                                          Mono.Call.Sites > 0 & Poly.Call.Sites > 0 & Freq.Poly.Call.Sites < 1.5 ~ "Polymorphic.Small"))
  return(df)
}

cluster_styling <- function(kable_table, df) {
  style <- kable_table %>%
    kableExtra::kable_styling(latex_table_env="tabularx") %>%
    kableExtra::row_spec(which(df$Cluster.Type == "Polymorphic.Small"), background = "blue1light") %>%
    kableExtra::row_spec(which(df$Cluster.Type == "Polymorphic.Medium"), background = "blue1medium") %>%
    kableExtra::row_spec(which(df$Cluster.Type == "Megamorphic"), background = "blue1dark")
  return(style)
}

add_lookup_status <- function(df_p, type) {
  df <- df_p %>%
    ungroup() %>%
    group_by_at(c(type, "Benchmark")) %>%
    dplyr::mutate(Cache.Type = case_when(Num.Receiver == 1 ~ "MONO",
                                        Num.Receiver > 1 & Num.Receiver <= 8 ~ "POLY",
                                        Num.Receiver > 8 ~ "MEGA")) 
  return(df)
}

create_aggregate_table <- function(df_p, col_selection, key, value) {
  df <- df_p %>% 
    select_at(all_of(col_selection)) %>%
    dplyr::mutate(across(is.numeric, round, digits=1)) %>%
    tidyr::spread(key, value) %>%
    replace(is.na(.), 0) %>%
    janitor::adorn_totals("row") 
  return(df)
}





