check_invalid_transition <- function(df_p) {
    df <- df_p %>%
    dplyr::mutate(Invalid.Status = case_when(Cache.Type.Original == "MONO" & Num.Targets.Observed == "POLY" ~ 'TRUE',
                                           Cache.Type.Original == "MONO" & Num.Targets.Observed == "MEGA" ~ 'TRUE',
                                           Cache.Type.Original == "POLY" & Num.Targets.Observed == "MEGA" ~ 'TRUE')) 
    testit::assert("Changes of cache status are only allowed from high degree of polymoprhism to low degree of polymoprhism", nrow(df %>%  dplyr::filter(grepl('TRUE', Invalid.Status))) == 0)
    df <- dplyr::select(df, -Invalid.Status) #We can safely drop this column then
    return(df)
}


has_changed_status <- function(df_p, benchmark=NULL) {
  df <- df_p %>%
    group_by_at(c("Has.Changed.Status", benchmark)) %>%
    dplyr::rename(Value = Has.Changed.Status) %>%
    dplyr::summarise(Has.Changed.Status.Sites = n_distinct(Symbol, CT.Address, Source.Section, Call.Site.ID), Has.Changed.Status.Calls = n_distinct(Call.ID)) 
  return(df)
}

has_experienced_tp <- function(df_p, benchmark=NULL) {
  df <- df_p %>%
    group_by_at(c("Target.Polymorphism", benchmark)) %>%
    dplyr::rename(Value = Target.Polymorphism) %>%
    dplyr::summarise(Target.Polymorphism.Sites = n_distinct(Symbol, CT.Address, Source.Section, Call.Site.ID), Target.Polymorphism.Calls = n_distinct(Call.ID))
  return(df)
}

build_detailed_summary_tp <- function(df_p, benchmark=NULL) {
    df <- df_p %>%
      filter(Has.Changed.Status == TRUE)  %>%
      group_by_at(c(Call.Site.Target, "Num.Receiver.Original", "Num.Receiver.Observed")) %>% 
      dplyr::summarise("Num.Call.Sites" = n_distinct(Source.Section, Symbol, Call.Site.ID, CT.Address),  Num.Calls = n_distinct(Call.ID)) %>%
      group_by(Num.Receiver.Original, Num.Receiver.Observed) %>%
      dplyr::summarise("Num.Call.Sites" = n_distinct(Source.Section, Symbol, Call.Site.ID, CT.Address), Num.Calls = sum(Num.Calls)) %>%
      group_by_at(benchmark) %>%
      dplyr::mutate(Cumulative.Call.Sites = rev(cumsum(rev(Num.Call.Sites)))) %>%
      dplyr::mutate(Cumulative.Calls = rev(cumsum(rev(Num.Calls)))) %>%
      dplyr::mutate(Frequency = round(Num.Calls/sum(Num.Calls),7)*100)  %>%
      dplyr::mutate(Cumulative.Freq = rev(cumsum(rev(Frequency))))
    return(df)    
} 

