using System.ComponentModel.DataAnnotations;

namespace StudMap.Admin.Models
{
    public class MapModel
    {
        [Required]
        [Display(Name = "Name")]
        public string MapName { get; set; }
    }
}