module TZInfo
  module Definitions
    module Africa
      module Tripoli
        include TimezoneDefinition
        
        timezone 'Africa/Tripoli' do |tz|
          tz.offset :o0, 3164, 0, :LMT
          tz.offset :o1, 3600, 0, :CET
          tz.offset :o2, 3600, 3600, :CEST
          tz.offset :o3, 7200, 0, :EET
          
          tz.transition 1919, 12, :o1, 52322208409, 21600
          tz.transition 1951, 10, :o2, 58414405, 24
          tz.transition 1951, 12, :o1, 29208149, 12
          tz.transition 1953, 10, :o2, 58431829, 24
          tz.transition 1953, 12, :o1, 29216921, 12
          tz.transition 1955, 9, :o2, 58449131, 24
          tz.transition 1955, 12, :o1, 29225681, 12
          tz.transition 1958, 12, :o3, 58477667, 24
          tz.transition 1981, 12, :o1, 378684000
          tz.transition 1982, 3, :o2, 386463600
          tz.transition 1982, 9, :o1, 402271200
          tz.transition 1983, 3, :o2, 417999600
          tz.transition 1983, 9, :o1, 433807200
          tz.transition 1984, 3, :o2, 449622000
          tz.transition 1984, 9, :o1, 465429600
          tz.transition 1985, 4, :o2, 481590000
          tz.transition 1985, 9, :o1, 496965600
          tz.transition 1986, 4, :o2, 512953200
          tz.transition 1986, 10, :o1, 528674400
          tz.transition 1987, 3, :o2, 544230000
          tz.transition 1987, 9, :o1, 560037600
          tz.transition 1988, 3, :o2, 575852400
          tz.transition 1988, 9, :o1, 591660000
          tz.transition 1989, 3, :o2, 607388400
          tz.transition 1989, 9, :o1, 623196000
          tz.transition 1990, 5, :o3, 641775600
          tz.transition 1996, 9, :o1, 844034400
          tz.transition 1997, 4, :o2, 860108400
          tz.transition 1997, 10, :o3, 875916000
          tz.transition 2012, 11, :o1, 1352505600
          tz.transition 2013, 3, :o2, 1364515200
          tz.transition 2013, 10, :o1, 1382659200
          tz.transition 2014, 3, :o2, 1395964800
          tz.transition 2014, 10, :o1, 1414713600
          tz.transition 2015, 3, :o2, 1427414400
          tz.transition 2015, 10, :o1, 1446163200
          tz.transition 2016, 3, :o2, 1458864000
          tz.transition 2016, 10, :o1, 1477612800
          tz.transition 2017, 3, :o2, 1490918400
          tz.transition 2017, 10, :o1, 1509062400
          tz.transition 2018, 3, :o2, 1522368000
          tz.transition 2018, 10, :o1, 1540512000
          tz.transition 2019, 3, :o2, 1553817600
          tz.transition 2019, 10, :o1, 1571961600
          tz.transition 2020, 3, :o2, 1585267200
          tz.transition 2020, 10, :o1, 1604016000
          tz.transition 2021, 3, :o2, 1616716800
          tz.transition 2021, 10, :o1, 1635465600
          tz.transition 2022, 3, :o2, 1648166400
          tz.transition 2022, 10, :o1, 1666915200
          tz.transition 2023, 3, :o2, 1680220800
          tz.transition 2023, 10, :o1, 1698364800
          tz.transition 2024, 3, :o2, 1711670400
          tz.transition 2024, 10, :o1, 1729814400
          tz.transition 2025, 3, :o2, 1743120000
          tz.transition 2025, 10, :o1, 1761868800
          tz.transition 2026, 3, :o2, 1774569600
          tz.transition 2026, 10, :o1, 1793318400
          tz.transition 2027, 3, :o2, 1806019200
          tz.transition 2027, 10, :o1, 1824768000
          tz.transition 2028, 3, :o2, 1838073600
          tz.transition 2028, 10, :o1, 1856217600
          tz.transition 2029, 3, :o2, 1869523200
          tz.transition 2029, 10, :o1, 1887667200
          tz.transition 2030, 3, :o2, 1900972800
          tz.transition 2030, 10, :o1, 1919116800
          tz.transition 2031, 3, :o2, 1932422400
          tz.transition 2031, 10, :o1, 1951171200
          tz.transition 2032, 3, :o2, 1963872000
          tz.transition 2032, 10, :o1, 1982620800
          tz.transition 2033, 3, :o2, 1995321600
          tz.transition 2033, 10, :o1, 2014070400
          tz.transition 2034, 3, :o2, 2027376000
          tz.transition 2034, 10, :o1, 2045520000
          tz.transition 2035, 3, :o2, 2058825600
          tz.transition 2035, 10, :o1, 2076969600
          tz.transition 2036, 3, :o2, 2090275200
          tz.transition 2036, 10, :o1, 2109024000
          tz.transition 2037, 3, :o2, 2121724800
          tz.transition 2037, 10, :o1, 2140473600
          tz.transition 2038, 3, :o2, 4931017, 2
          tz.transition 2038, 10, :o1, 4931451, 2
          tz.transition 2039, 3, :o2, 4931745, 2
          tz.transition 2039, 10, :o1, 4932179, 2
          tz.transition 2040, 3, :o2, 4932487, 2
          tz.transition 2040, 10, :o1, 4932907, 2
          tz.transition 2041, 3, :o2, 4933215, 2
          tz.transition 2041, 10, :o1, 4933635, 2
          tz.transition 2042, 3, :o2, 4933943, 2
          tz.transition 2042, 10, :o1, 4934377, 2
          tz.transition 2043, 3, :o2, 4934671, 2
          tz.transition 2043, 10, :o1, 4935105, 2
          tz.transition 2044, 3, :o2, 4935399, 2
          tz.transition 2044, 10, :o1, 4935833, 2
          tz.transition 2045, 3, :o2, 4936141, 2
          tz.transition 2045, 10, :o1, 4936561, 2
          tz.transition 2046, 3, :o2, 4936869, 2
          tz.transition 2046, 10, :o1, 4937289, 2
          tz.transition 2047, 3, :o2, 4937597, 2
          tz.transition 2047, 10, :o1, 4938017, 2
          tz.transition 2048, 3, :o2, 4938325, 2
          tz.transition 2048, 10, :o1, 4938759, 2
          tz.transition 2049, 3, :o2, 4939053, 2
          tz.transition 2049, 10, :o1, 4939487, 2
          tz.transition 2050, 3, :o2, 4939781, 2
          tz.transition 2050, 10, :o1, 4940215, 2
        end
      end
    end
  end
end
