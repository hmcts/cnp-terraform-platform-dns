module "record" {
  source         = "git::https://github.com/hmcts/moj-terraform-module-dns.git//module/A"
  name           = "${var.name}"
  zone           = "${var.zone}"
  resource_group = "${var.resource_group}"
  destination    = ["${var.destination}"]
}
