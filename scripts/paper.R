vm_names <- c(
  "GraalBasic"            = "Graal",
  "GraalEnterprise"       = "Graal VM",
  
  "Java8U66"              = "Java",
  
  "Node"                  = "Node.js",
  "SOMns-no-tracing"      = "SOMns",
  "SOMns"                 = "SOMns",
  "SOMns-Enterprise"      = "SOMns GraalVM",
  
  "AkkaActor"    = "Akka",
  "JetlangActor" = "Jetlang",
  "ScalazActor"  = "Scalaz")

vms_all <- names(vm_names)

# vm_colors <- brewer.pal(length(vms_all), "Paired")  # to replace scale_fill_brewer(type = "qual", palette = "Paired")
vm_colors <- rainbow(length(vms_all))

names(vm_colors) <- vm_names

per <- function (x) {
  round((x - 1) * 100, 0)
}

X0 <- function(x) {
  round(x, 0)
}

X1 <- function(x) {
  round(x, 1)
}

X2 <- function(x) {
  round(x, 2)
}
